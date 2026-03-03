package com.nrkgo.accounts.modules.plans.admin;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.model.Plan;
import com.nrkgo.accounts.modules.plans.model.Subscription;
import com.nrkgo.accounts.modules.plans.repository.PlanRepository;
import com.nrkgo.accounts.modules.plans.repository.SubscriptionRepository;
import com.nrkgo.accounts.modules.plans.service.DefaultPlanService;
import com.nrkgo.accounts.repository.OrganizationRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only endpoints for managing org plans.
 * Access controlled by app.admin.emails in application.properties.
 * These are product super-admins (product owners), NOT org-level admins.
 *
 * Plans are org-scoped: all users in an org share the org's plan.
 */
@RestController
@RequestMapping("/admin/plans")
public class AdminPlanController {

    private final AdminGuard adminGuard;
    private final UserService userService;
    private final OrganizationRepository organizationRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminPlanController(AdminGuard adminGuard,
            UserService userService,
            OrganizationRepository organizationRepository,
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository) {
        this.adminGuard = adminGuard;
        this.userService = userService;
        this.organizationRepository = organizationRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        for (Cookie cookie : request.getCookies()) {
            if ("user_session".equals(cookie.getName())) {
                return userService.getUserBySession(cookie.getValue());
            }
        }
        return null;
    }

    /**
     * POST /admin/plans/assign
     *
     * Assign or update a plan for an organization.
     *
     * Case 1 — payload plan_name matches org's current active plan:
     * → Update limits only (updates locked_features_json on the current row).
     * Use: "Give this org extra guides without changing their plan name."
     *
     * Case 2 — payload plan_name is different from current plan:
     * → Full plan switch (expire old row, insert new subscription row).
     * Use: "Upgrade this org from Free to Pro."
     */
    @PostMapping("/assign")
    @Transactional
    public ResponseEntity<ApiResponse<Subscription>> assignPlan(
            HttpServletRequest request,
            @RequestBody AdminPlanRequest payload) {

        // 1. Auth + admin guard
        User admin = getAuthenticatedUser(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        // 2. Validate required fields
        if (payload.getOrgId() == null || payload.getProductCode() == null || payload.getPlanName() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("org_id, product_code, and plan_name are required"));
        }

        // 3. Load the org
        Organization org = organizationRepository.findById(payload.getOrgId()).orElse(null);
        if (org == null)
            return ResponseEntity.status(404).body(ApiResponse.error("Org not found: " + payload.getOrgId()));

        // 4. Load the target plan
        Plan targetPlan = planRepository
                .findByProductCodeAndPlanNameAndStatus(payload.getProductCode(), payload.getPlanName(), 1)
                .orElse(null);
        if (targetPlan == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error(
                            "Plan '" + payload.getPlanName() + "' not found for product " + payload.getProductCode()));
        }

        // 5. Get current active subscription
        Subscription currentSub = subscriptionRepository
                .findFirstByOrgIdAndProductCodeAndStatusOrderByCreatedTimeDesc(
                        org.getId(), payload.getProductCode(), 1)
                .orElse(null);

        String currentPlanName = (currentSub != null) ? currentSub.getPlan().getPlanName() : null;
        boolean isSamePlan = targetPlan.getPlanName().equals(currentPlanName);

        Subscription result;

        if (isSamePlan && currentSub != null) {
            // ── CASE 1: Same plan → Update limits only ──
            String newLimits = payload.getCustomLimitsJson() != null
                    ? payload.getCustomLimitsJson()
                    : targetPlan.getFeaturesJson();

            currentSub.setLockedFeaturesJson(newLimits);
            currentSub.setModifiedTime(System.currentTimeMillis());
            result = subscriptionRepository.save(currentSub);

        } else {
            // ── CASE 2: Different plan → Full plan switch ──
            if (currentSub != null) {
                currentSub.setStatus(0);
                currentSub.setModifiedTime(System.currentTimeMillis());
                subscriptionRepository.save(currentSub);
            }

            long now = System.currentTimeMillis();
            Subscription newSub = new Subscription();
            newSub.setOrg(org);
            newSub.setProductCode(payload.getProductCode());
            newSub.setPlan(targetPlan);
            newSub.setPreviousPlanId(currentSub != null ? currentSub.getPlan().getId() : null);
            newSub.setActivationSource(DefaultPlanService.SOURCE_ADMIN);
            newSub.setActivatedBy(admin.getId());
            newSub.setStatus(1);
            newSub.setStartTime(now);
            newSub.setExpiryTime(payload.getExpiryTime() != null ? payload.getExpiryTime() : -1L);
            newSub.setCreatedTime(now);
            newSub.setModifiedTime(now);

            String lockedLimits = payload.getCustomLimitsJson() != null
                    ? payload.getCustomLimitsJson()
                    : targetPlan.getFeaturesJson();
            newSub.setLockedFeaturesJson(lockedLimits);

            result = subscriptionRepository.save(newSub);
        }

        String msg = isSamePlan
                ? "Limits updated for org: " + org.getOrgName()
                : "Plan switched to '" + targetPlan.getPlanName() + "' for org: " + org.getOrgName();

        return ResponseEntity.ok(ApiResponse.success(msg, result));
    }

    /**
     * GET /admin/plans/history?orgId=5&productCode=101
     * Full plan history for an org.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getHistory(
            HttpServletRequest request,
            @RequestParam Long orgId,
            @RequestParam Integer productCode) {

        User admin = getAuthenticatedUser(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        var history = subscriptionRepository.findByOrgIdAndProductCodeOrderByCreatedTimeDesc(orgId, productCode);
        return ResponseEntity.ok(ApiResponse.success("Subscription history", history));
    }
}

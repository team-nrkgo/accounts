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

import java.util.List;

/**
 * Admin endpoints for plan definitions and org plan assignments.
 *
 * ── Plan Definition CRUD ──────────────────────────────────────────────────
 * GET /admin/plans/list?productCode=101 List all plans for a product
 * POST /admin/plans/create Create a new plan definition
 * PUT /admin/plans/{planId} Update plan features/price (new subscribers only)
 * DELETE /admin/plans/{planId} Deprecate a plan (soft delete)
 *
 * ── Org Plan Assignments ──────────────────────────────────────────────────
 * POST /admin/plans/assign Assign/switch an org's plan
 * GET /admin/plans/history View full plan history for an org
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

    // ════════════════════════════════════════════════════════════════════════
    // Plan Definition CRUD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/plans/list?productCode=101
     * List all plan definitions for a product (active + deprecated).
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Plan>>> listPlans(
            HttpServletRequest request,
            @RequestParam Integer productCode) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        return ResponseEntity.ok(ApiResponse.success("Plans", planRepository.findByProductCode(productCode)));
    }

    /**
     * POST /admin/plans/create
     * Add a new plan definition for a product.
     *
     * Body: { "product_code": 101, "plan_name": "Enterprise", "plan_type": 2,
     * "price": 49.00, "currency": "USD",
     * "features_json": "{\"max_guides\": -1, \"cloud_storage\": true}" }
     */
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<ApiResponse<Plan>> createPlan(
            HttpServletRequest request,
            @RequestBody AdminPlanDefinitionRequest payload) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        if (payload.getProductCode() == null || payload.getPlanName() == null
                || payload.getPlanType() == null || payload.getFeaturesJson() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("product_code, plan_name, plan_type, and features_json are required"));
        }

        long now = System.currentTimeMillis();
        Plan plan = new Plan();
        plan.setProductCode(payload.getProductCode());
        plan.setPlanName(payload.getPlanName());
        plan.setPlanType(payload.getPlanType());
        plan.setPrice(payload.getPrice());
        plan.setCurrency(payload.getCurrency() != null ? payload.getCurrency() : "USD");
        plan.setFeaturesJson(payload.getFeaturesJson());
        plan.setStatus(1);
        plan.setCreatedTime(now);
        plan.setModifiedTime(now);

        return ResponseEntity.ok(ApiResponse.success("Plan created", planRepository.save(plan)));
    }

    /**
     * PUT /admin/plans/{planId}
     * Update a plan's features/price. Only affects NEW subscribers.
     * Existing paid orgs are protected by locked_features_json (grandfathering).
     */
    @PutMapping("/{planId}")
    @Transactional
    public ResponseEntity<ApiResponse<Plan>> updatePlan(
            HttpServletRequest request,
            @PathVariable Long planId,
            @RequestBody AdminPlanDefinitionRequest payload) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        Plan plan = planRepository.findById(planId).orElse(null);
        if (plan == null)
            return ResponseEntity.status(404).body(ApiResponse.error("Plan not found: " + planId));

        if (payload.getPlanName() != null)
            plan.setPlanName(payload.getPlanName());
        if (payload.getPlanType() != null)
            plan.setPlanType(payload.getPlanType());
        if (payload.getPrice() != null)
            plan.setPrice(payload.getPrice());
        if (payload.getCurrency() != null)
            plan.setCurrency(payload.getCurrency());
        if (payload.getFeaturesJson() != null)
            plan.setFeaturesJson(payload.getFeaturesJson());
        plan.setModifiedTime(System.currentTimeMillis());

        return ResponseEntity.ok(ApiResponse.success(
                "Plan updated. Existing paid orgs are unaffected (grandfathered).",
                planRepository.save(plan)));
    }

    /**
     * DELETE /admin/plans/{planId}
     * Soft delete: marks the plan as deprecated (status=0).
     * Existing orgs on this plan keep it — their subscription rows still reference
     * it.
     */
    @DeleteMapping("/{planId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deprecatePlan(
            HttpServletRequest request,
            @PathVariable Long planId) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        Plan plan = planRepository.findById(planId).orElse(null);
        if (plan == null)
            return ResponseEntity.status(404).body(ApiResponse.error("Plan not found: " + planId));

        plan.setStatus(0);
        plan.setModifiedTime(System.currentTimeMillis());
        planRepository.save(plan);

        return ResponseEntity.ok(ApiResponse.success("Plan deprecated. Existing subscribers unaffected.", null));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Org Plan Assignments
    // ════════════════════════════════════════════════════════════════════════

    /**
     * POST /admin/plans/assign
     * Smart plan assignment for an org:
     * - Same plan name as current → update limits only (custom_limits_json)
     * - Different plan name → full plan switch
     */
    @PostMapping("/assign")
    @Transactional
    public ResponseEntity<ApiResponse<Subscription>> assignPlan(
            HttpServletRequest request,
            @RequestBody AdminPlanRequest payload) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        if (payload.getOrgId() == null || payload.getProductCode() == null || payload.getPlanName() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("org_id, product_code, and plan_name are required"));
        }

        Organization org = organizationRepository.findById(payload.getOrgId()).orElse(null);
        if (org == null)
            return ResponseEntity.status(404).body(ApiResponse.error("Org not found: " + payload.getOrgId()));

        Plan targetPlan = planRepository
                .findByProductCodeAndPlanNameAndStatus(payload.getProductCode(), payload.getPlanName(), 1)
                .orElse(null);
        if (targetPlan == null)
            return ResponseEntity.status(404).body(
                    ApiResponse.error(
                            "Plan '" + payload.getPlanName() + "' not found for product " + payload.getProductCode()));

        Subscription currentSub = subscriptionRepository
                .findFirstByOrgIdAndProductCodeAndStatusOrderByCreatedTimeDesc(org.getId(), payload.getProductCode(), 1)
                .orElse(null);

        String currentPlanName = (currentSub != null) ? currentSub.getPlan().getPlanName() : null;
        boolean isSamePlan = targetPlan.getPlanName().equals(currentPlanName);
        Subscription result;

        if (isSamePlan && currentSub != null) {
            // Same plan → update limits only
            String newLimits = payload.getCustomLimitsJson() != null
                    ? payload.getCustomLimitsJson()
                    : targetPlan.getFeaturesJson();
            currentSub.setLockedFeaturesJson(newLimits);
            currentSub.setModifiedTime(System.currentTimeMillis());
            result = subscriptionRepository.save(currentSub);
        } else {
            // Different plan → full switch
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
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getHistory(
            HttpServletRequest request,
            @RequestParam Long orgId,
            @RequestParam Integer productCode) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        return ResponseEntity.ok(ApiResponse.success("Subscription history",
                subscriptionRepository.findByOrgIdAndProductCodeOrderByCreatedTimeDesc(orgId, productCode)));
    }

    private User auth(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        for (Cookie c : request.getCookies()) {
            if ("user_session".equals(c.getName()))
                return userService.getUserBySession(c.getValue());
        }
        return null;
    }
}

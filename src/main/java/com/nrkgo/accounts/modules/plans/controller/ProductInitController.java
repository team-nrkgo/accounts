package com.nrkgo.accounts.modules.plans.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.dto.InitResponse;
import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.dto.ProductInitResponse;
import com.nrkgo.accounts.modules.plans.model.Product;
import com.nrkgo.accounts.modules.plans.model.Subscription;
import com.nrkgo.accounts.modules.plans.repository.ProductRepository;
import com.nrkgo.accounts.modules.plans.repository.SubscriptionRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Product-specific init endpoint.
 *
 * GET /{product}/init
 * Example: GET /snapsteps/init
 *
 * Returns a single merged payload:
 * - user_information (from existing getInitData)
 * - default_organization (from existing getInitData)
 * - other_organizations (from existing getInitData)
 * - plan (from subscriptions table for this product + org)
 *
 * The "product" path variable is resolved to a product code via DB lookup on
 * the
 * products table (slug column). Adding a new product: just INSERT a row into
 * the products table — no Java code changes needed.
 */
@RestController
public class ProductInitController {

    private final UserService userService;
    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public ProductInitController(UserService userService,
            ProductRepository productRepository,
            SubscriptionRepository subscriptionRepository,
            ObjectMapper objectMapper) {
        this.userService = userService;
        this.productRepository = productRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * GET /{product}/init?orgId=5
     *
     * @param product URL slug of the product (e.g. "snapsteps")
     * @param orgId   Optional. Which org context to use. Defaults to the user's
     *                default org.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping({ "/{product}/init", "/api/{product}/init" })
    public ResponseEntity<ApiResponse<ProductInitResponse>> productInit(
            @PathVariable String product,
            @RequestParam(required = false) Long orgId,
            HttpServletRequest request) {

        // 1. Auth
        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        // 2. Resolve product code from slug via DB (not hardcoded Java map)
        Product productDef = productRepository.findBySlugAndStatus(product, 1).orElse(null);
        if (productDef == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Unknown product: " + product));
        }
        Integer productCode = productDef.getProductCode();

        // 3. Fetch base init data (user, default org, other orgs) — reuse existing
        // service
        InitResponse baseInit = userService.getInitData(user.getId(), orgId);

        // 4. Get the org's active plan for this product
        Organization defaultOrg = baseInit.getDefaultOrganizations();
        Subscription sub = null;
        if (defaultOrg != null) {
            sub = subscriptionRepository
                    .findFirstByOrgIdAndProductCodeAndStatusOrderByCreatedTimeDesc(
                            defaultOrg.getId(), productCode, 1)
                    .orElse(null);
        }

        // 5. Merge into ProductInitResponse
        ProductInitResponse response = new ProductInitResponse();
        response.setUserInformation(baseInit.getUserInformation());
        response.setDefaultOrganization(baseInit.getDefaultOrganizations());
        response.setOtherOrganizations(baseInit.getOtherOrganizations());

        // 6. Build plan summary
        if (sub != null) {
            ProductInitResponse.PlanSummary planSummary = new ProductInitResponse.PlanSummary();
            planSummary.setPlanName(sub.getPlan().getPlanName());
            planSummary.setPlanType(sub.getPlan().getPlanType());
            planSummary.setExpiryTime(sub.getExpiryTime());
            planSummary.setActivationSource(sub.getActivationSource());

            // Parse features_json into a map so it's returned as a proper JSON object
            // Priority: lockedFeaturesJson (paid users) → plan featuresJson (free users)
            String featuresJson = sub.getLockedFeaturesJson() != null
                    ? sub.getLockedFeaturesJson()
                    : sub.getPlan().getFeaturesJson();

            try {
                Map<String, Object> featuresMap = objectMapper.readValue(
                        featuresJson, new TypeReference<Map<String, Object>>() {
                        });
                planSummary.setFeatures(featuresMap);
            } catch (Exception e) {
                planSummary.setFeatures(featuresJson); // fallback: raw string
            }

            response.setPlan(planSummary);
        } else {
            // No subscription found — org hasn't been initialized yet
            response.setPlan(null);
        }

        return ResponseEntity.ok(ApiResponse.success("Init data for " + product, response));
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
}

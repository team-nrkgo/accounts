package com.nrkgo.accounts.modules.plans.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.model.Plan;
import com.nrkgo.accounts.modules.plans.model.Subscription;
import com.nrkgo.accounts.modules.plans.repository.PlanRepository;
import com.nrkgo.accounts.modules.plans.repository.ProductRepository;
import com.nrkgo.accounts.modules.plans.service.PlanService;
import com.nrkgo.accounts.modules.plans.service.PlanServiceFactory;
import com.nrkgo.accounts.modules.plans.service.ProductCodes;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanServiceFactory planServiceFactory;
    private final PlanRepository planRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public PlanController(PlanServiceFactory planServiceFactory,
            PlanRepository planRepository,
            ProductRepository productRepository,
            UserService userService) {
        this.planServiceFactory = planServiceFactory;
        this.planRepository = planRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null)
            return null;
        return userService.getUserBySession(token);
    }

    /**
     * GET /api/plans/status?productCode=101
     * Returns the active subscription for the logged-in user for a product.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Subscription>> getStatus(
            HttpServletRequest request,
            @RequestParam(defaultValue = "101") int productCode) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        PlanService service = planServiceFactory.getInstance(productCode);
        Subscription sub = service.getActiveSubscription(user);

        if (sub == null)
            return ResponseEntity.ok(ApiResponse.success("No active subscription", null));
        return ResponseEntity.ok(ApiResponse.success("Active subscription", sub));
    }

    /**
     * GET /api/plans/list?productCode=101
     * Returns all available plans for a product (for showing pricing page).
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Plan>>> listPlans(
            @RequestParam(defaultValue = "101") int productCode) {

        var product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown product code: " + productCode));

        List<Plan> plans = planRepository.findByProductIdAndStatus(product.getId(), 1);
        return ResponseEntity.ok(ApiResponse.success("Plans", plans));
    }

    /**
     * POST /api/plans/init?productCode=101
     * Initializes the free plan for a user. Called after signup.
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Subscription>> initFreePlan(
            HttpServletRequest request,
            @RequestParam(defaultValue = "101") int productCode) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        PlanService service = planServiceFactory.getInstance(productCode);
        Subscription sub = service.initFreePlan(user);
        return ResponseEntity.ok(ApiResponse.success("Free plan initialized", sub));
    }

    /**
     * POST /api/plans/switch
     * Switches user to a different plan. Body: {"planId": 2, "productCode": 101}
     */
    @PostMapping("/switch")
    public ResponseEntity<ApiResponse<Subscription>> switchPlan(
            HttpServletRequest request,
            @RequestBody Map<String, Integer> body) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        int productCode = body.getOrDefault("productCode", ProductCodes.SNAP_STEPS);
        Long planId = body.get("planId").longValue();

        PlanService service = planServiceFactory.getInstance(productCode);
        Subscription sub = service.switchPlan(user, planId);
        return ResponseEntity.ok(ApiResponse.success("Plan switched", sub));
    }

    /**
     * POST /api/plans/cancel?productCode=101
     * Cancels current plan and falls back to Free.
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            HttpServletRequest request,
            @RequestParam(defaultValue = "101") int productCode) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        PlanService service = planServiceFactory.getInstance(productCode);
        service.cancelSubscription(user);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled and downgraded to free", null));
    }
}

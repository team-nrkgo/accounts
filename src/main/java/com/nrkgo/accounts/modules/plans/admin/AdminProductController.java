package com.nrkgo.accounts.modules.plans.admin;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.model.Product;
import com.nrkgo.accounts.modules.plans.repository.ProductRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin CRUD for the products registry.
 *
 * GET /admin/products → list all products
 * POST /admin/products → register a new product
 * PUT /admin/products/{code} → update product name/slug/description
 * DELETE /admin/products/{code} → deprecate a product (soft delete, status=0)
 */
@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminGuard adminGuard;
    private final UserService userService;
    private final ProductRepository productRepository;

    public AdminProductController(AdminGuard adminGuard,
            UserService userService,
            ProductRepository productRepository) {
        this.adminGuard = adminGuard;
        this.userService = userService;
        this.productRepository = productRepository;
    }

    // ── GET /admin/products ─────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> listProducts(HttpServletRequest request) {
        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        return ResponseEntity.ok(ApiResponse.success("Products", productRepository.findAll()));
    }

    // ── POST /admin/products ────────────────────────────────────────────────
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Product>> createProduct(
            HttpServletRequest request,
            @RequestBody AdminProductRequest payload) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        if (payload.getProductCode() == null || payload.getSlug() == null || payload.getName() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("product_code, slug, and name are required"));
        }

        // Duplicate check (DB also enforces UNIQUE, but give a clean error message)
        if (productRepository.existsByProductCode(payload.getProductCode())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Product code " + payload.getProductCode() + " is already registered"));
        }

        Product product = new Product();
        product.setProductCode(payload.getProductCode());
        product.setSlug(payload.getSlug().toLowerCase().trim());
        product.setName(payload.getName());
        product.setDescription(payload.getDescription());
        product.setStatus(1);
        product.setCreatedTime(System.currentTimeMillis());

        return ResponseEntity.ok(ApiResponse.success("Product registered", productRepository.save(product)));
    }

    // ── PUT /admin/products/{productCode} ───────────────────────────────────
    @PutMapping("/{productCode}")
    @Transactional
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            HttpServletRequest request,
            @PathVariable Integer productCode,
            @RequestBody AdminProductRequest payload) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        Product product = productRepository.findByProductCodeAndStatus(productCode, 1).orElse(null);
        if (product == null)
            return ResponseEntity.status(404).body(ApiResponse.error("Product not found: " + productCode));

        if (payload.getSlug() != null)
            product.setSlug(payload.getSlug().toLowerCase().trim());
        if (payload.getName() != null)
            product.setName(payload.getName());
        if (payload.getDescription() != null)
            product.setDescription(payload.getDescription());

        return ResponseEntity.ok(ApiResponse.success("Product updated", productRepository.save(product)));
    }

    // ── DELETE /admin/products/{productCode} ────────────────────────────────
    @DeleteMapping("/{productCode}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deprecateProduct(
            HttpServletRequest request,
            @PathVariable Integer productCode) {

        User admin = auth(request);
        if (admin == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        try {
            adminGuard.requireAdmin(admin);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }

        Product product = productRepository.findByProductCodeAndStatus(productCode, 1).orElse(null);
        if (product == null)
            return ResponseEntity.status(404).body(ApiResponse.error("Product not found: " + productCode));

        product.setStatus(0); // Soft delete — preserves all subscription history
        productRepository.save(product);

        return ResponseEntity.ok(ApiResponse
                .success("Product deprecated (soft delete). All existing subscriptions are unaffected.", null));
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

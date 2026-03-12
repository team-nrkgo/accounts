package com.nrkgo.accounts.modules.echo.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.model.EchoCategory;
import com.nrkgo.accounts.modules.echo.model.EchoTag;
import com.nrkgo.accounts.modules.echo.repository.EchoCategoryRepository;
import com.nrkgo.accounts.modules.echo.repository.EchoTagRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/echo")
public class EchoMetadataController {

    private final EchoCategoryRepository categoryRepository;
    private final EchoTagRepository tagRepository;
    private final UserService userService;

    public EchoMetadataController(EchoCategoryRepository categoryRepository,
            EchoTagRepository tagRepository,
            UserService userService) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
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

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<EchoCategory>>> getCategories(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        List<EchoCategory> categories = categoryRepository.findByOrgId(orgId);
        return ResponseEntity.ok(ApiResponse.success("Categories fetched", categories));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<EchoCategory>> createCategory(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestBody Map<String, String> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        String name = payload.get("name");
        if (name == null || name.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));

        String slug = name.toLowerCase().replaceAll("[^a-z0-9]", "-");

        EchoCategory category = categoryRepository.findByOrgIdAndSlug(orgId, slug)
                .orElseGet(() -> {
                    EchoCategory newCat = new EchoCategory();
                    newCat.setOrgId(orgId);
                    newCat.setName(name);
                    newCat.setSlug(slug);
                    return categoryRepository.save(newCat);
                });

        return ResponseEntity.ok(ApiResponse.success("Category created/retrieved", category));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<EchoTag>>> getTags(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        List<EchoTag> tags = tagRepository.findByOrgId(orgId);
        return ResponseEntity.ok(ApiResponse.success("Tags fetched", tags));
    }
}

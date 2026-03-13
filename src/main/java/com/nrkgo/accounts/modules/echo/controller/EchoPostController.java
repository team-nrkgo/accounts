package com.nrkgo.accounts.modules.echo.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import com.nrkgo.accounts.modules.echo.model.EchoPost;
import com.nrkgo.accounts.modules.echo.service.EchoPostService;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class EchoPostController {

    private final EchoPostService service;
    private final UserService userService;
    private final com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository;

    public EchoPostController(EchoPostService service, UserService userService,
            com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository) {
        this.service = service;
        this.userService = userService;
        this.orgUserRepository = orgUserRepository;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_session".equals(cookie.getName())) {
                    return userService.getUserBySession(cookie.getValue());
                }
            }
        }
        return null;
    }

    @GetMapping("/api/echo/ping")
    public String ping() {
        return "pong";
    }

    private void validateOrganizationMembership(User user, Long orgId) {
        if (orgId == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        boolean isMember = orgUserRepository.findByUserId(user.getId()).stream()
                .anyMatch(ou -> ou.getOrgId().equals(orgId));
        if (!isMember) {
            throw new SecurityException("Access Denied: You are not a member of this organization");
        }
    }

    @PostMapping("/api/echo/posts")
    public ResponseEntity<ApiResponse<EchoPostDto>> createPost(
            HttpServletRequest request,
            @RequestParam(name = "org_id", required = false) Long orgIdParam,
            @RequestBody EchoPostDto dto) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        Long orgId = orgIdParam != null ? orgIdParam : dto.getOrgId();

        try {
            validateOrganizationMembership(user, orgId);
            if (dto.getOrgId() == null)
                dto.setOrgId(orgId);

            EchoPostDto post = service.createPost(dto, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Post created successfully", post));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/api/echo/posts")
    public ResponseEntity<ApiResponse<?>> listPosts(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "15") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            validateOrganizationMembership(user, orgId);
            Pageable pageable = PageRequest.of(page, size, Sort.by("modifiedTime").descending());
            Page<EchoPostDto> posts = service.listPosts(orgId, user, status, search, pageable);
            return ResponseEntity.ok(ApiResponse.paginatedSuccess("Posts fetched successfully", posts));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/api/echo/posts/{id}")
    public ResponseEntity<ApiResponse<EchoPostDto>> getPost(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            validateOrganizationMembership(user, orgId);
            EchoPostDto post = service.getPostById(id, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Post details fetched", post));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/api/echo/posts/{id}")
    public ResponseEntity<ApiResponse<EchoPostDto>> updatePost(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(name = "org_id", required = false) Long orgIdParam,
            @RequestBody EchoPostDto dto) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        Long orgId = orgIdParam != null ? orgIdParam : dto.getOrgId();

        try {
            validateOrganizationMembership(user, orgId);
            if (dto.getOrgId() == null)
                dto.setOrgId(orgId);

            EchoPostDto post = service.updatePost(id, dto, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Post updated successfully", post));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/api/echo/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            validateOrganizationMembership(user, orgId);
            service.deletePost(id, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Post moved to trash", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/api/echo/posts/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            validateOrganizationMembership(user, orgId);
            service.changeStatus(id, status, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Status updated to " + status, null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/api/echo/public/posts")
    public ResponseEntity<ApiResponse<?>> listPublicPosts(
            @RequestParam(name = "org_id") Long orgId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("modifiedTime").descending());
            Page<EchoPostDto> posts = service.listPublishedPosts(orgId, pageable);
            return ResponseEntity.ok(ApiResponse.paginatedSuccess("Public posts fetched", posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/api/echo/public/posts/{slug}")
    public ResponseEntity<ApiResponse<EchoPostDto>> getPublicPost(
            @PathVariable String slug,
            @RequestParam(name = "org_id") Long orgId) {

        try {
            EchoPostDto post = service.getPublishedPostBySlug(orgId, slug);
            return ResponseEntity.ok(ApiResponse.success("Public post fetched", post));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/api/echo/posts/{id}/featured-image")
    public ResponseEntity<ApiResponse<String>> uploadFeaturedImage(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);
            String imageUrl = service.uploadFeaturedImage(id, file, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Featured image uploaded successfully", imageUrl));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}

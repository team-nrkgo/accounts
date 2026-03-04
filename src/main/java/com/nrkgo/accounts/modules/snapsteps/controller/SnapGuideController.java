package com.nrkgo.accounts.modules.snapsteps.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import com.nrkgo.accounts.modules.snapsteps.service.SnapGuideService;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/snapsteps/api/guides")
public class SnapGuideController {

    private final SnapGuideService guideService;
    private final UserService userService;
    private final com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository;

    @Value("${app.guides.default-limit:30}")
    private int defaultLimit;

    public SnapGuideController(SnapGuideService guideService,
            UserService userService,
            com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository) {
        this.guideService = guideService;
        this.userService = userService;
        this.orgUserRepository = orgUserRepository;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String token = getCookieValue(request, "user_session");
        if (token == null)
            return null;
        return userService.getUserBySession(token);
    }

    private Long getOrganizationId(HttpServletRequest request, User user) {
        String orgIdStr = getCookieValue(request, "ORG_ID");
        if (orgIdStr != null) {
            try {
                return Long.parseLong(orgIdStr);
            } catch (NumberFormatException ignored) {
            }
        }

        // Fallback: Use user's default organization
        return orgUserRepository.findByUserId(user.getId()).stream()
                .filter(ou -> ou.getIsDefault() != null && ou.getIsDefault() == 1)
                .map(com.nrkgo.accounts.model.OrgUser::getOrgId)
                .findFirst()
                .orElseGet(() -> orgUserRepository.findByUserId(user.getId()).stream()
                        .map(com.nrkgo.accounts.model.OrgUser::getOrgId)
                        .findFirst()
                        .orElse(null));
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<SnapGuide>> saveGuide(
            HttpServletRequest request,
            @RequestBody SnapGuideDto guideDto) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Long orgId = getOrganizationId(request, user);
        if (orgId == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Organization context missing"));
        }

        try {
            SnapGuide savedGuide = guideService.saveGuide(guideDto, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Guide saved successfully", savedGuide));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to save guide: " + e.getMessage()));
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> listGuides(
            HttpServletRequest request,
            @RequestParam(value = "id", required = false) Long guideId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {

        System.out.println("DEBUG: listGuides called with id=" + guideId + ", page=" + page + ", search=" + search);

        // Fallback for ID if @RequestParam fails
        if (guideId == null && request.getParameter("id") != null) {
            try {
                guideId = Long.parseLong(request.getParameter("id"));
                System.out.println("DEBUG: Extracted id manually from request: " + guideId);
            } catch (Exception ignored) {
            }
        }

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Long orgId = getOrganizationId(request, user);
        if (orgId == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Organization context missing"));
        }

        // Single Record Detail Flow (by ID)
        if (guideId != null) {
            try {
                System.out.println("DEBUG: Fetching single guide with ID: " + guideId);
                SnapGuide guide = guideService.getGuideByNumericId(guideId, user, orgId);
                return ResponseEntity.ok(ApiResponse.success("Guide details fetched", guide));
            } catch (SecurityException e) {
                return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
        }

        // Paginated Flow
        if (page != null) {
            int springPage = (page > 0) ? page - 1 : 0; // 1-based to 0-based
            int pageSize = (size != null) ? size : defaultLimit;
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                    springPage,
                    pageSize,
                    org.springframework.data.domain.Sort.by("modifiedTime").descending());

            org.springframework.data.domain.Page<SnapGuide> result;
            if (search != null && !search.trim().isEmpty()) {
                result = guideService.searchGuides(user, orgId, search, pageable);
            } else {
                result = guideService.getGuidesForUser(user, orgId, pageable);
            }
            return ResponseEntity.ok(ApiResponse.paginatedSuccess("Guides fetched successfully", result));
        }

        // Default: Full list Flow (Backward compatibility)
        List<SnapGuide> guides = guideService.getGuidesForUser(user, orgId);
        return ResponseEntity.ok(ApiResponse.success("Guides fetched successfully", guides));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/{externalId}")
    public ResponseEntity<ApiResponse<SnapGuide>> getGuide(
            HttpServletRequest request,
            @PathVariable String externalId) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Long orgId = getOrganizationId(request, user);
        if (orgId == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Organization context missing"));
        }

        try {
            SnapGuide guide = guideService.getGuideById(externalId, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Guide details fetched", guide));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGuide(
            HttpServletRequest request,
            @PathVariable String externalId) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Long orgId = getOrganizationId(request, user);
        if (orgId == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Organization context missing"));
        }

        try {
            guideService.deleteGuide(externalId, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Guide deleted successfully", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}

package com.nrkgo.accounts.modules.snapsteps.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import com.nrkgo.accounts.modules.snapsteps.service.SnapGuideService;
import com.nrkgo.accounts.modules.snapsteps.service.SnapTicketService;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapTicketDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapTicket;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/snapsteps/api")
public class SnapGuideController {

    private final SnapGuideService guideService;
    private final UserService userService;
    private final com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository;

    @Value("${app.guides.default-limit:30}")
    private int defaultLimit;

    private final SnapTicketService ticketService;

    public SnapGuideController(SnapGuideService guideService,
            UserService userService,
            com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository,
            SnapTicketService ticketService) {
        this.guideService = guideService;
        this.userService = userService;
        this.orgUserRepository = orgUserRepository;
        this.ticketService = ticketService;
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

    @PostMapping({ "/guides/save", "/guides" })
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
    @GetMapping("/guides")
    public ResponseEntity<ApiResponse<?>> listGuides(
            HttpServletRequest request,
            @RequestParam(value = "id", required = false) Long guideId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean starred) {

        System.out.println("DEBUG: listGuides called with id=" + guideId + ", page=" + page + ", search=" + search
                + ", starred=" + starred);

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
            if (starred != null && starred) {
                result = guideService.getStarredGuides(user, orgId, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                result = guideService.searchGuides(user, orgId, search, pageable);
            } else {
                result = guideService.getGuidesForUser(user, orgId, pageable);
            }
            return ResponseEntity.ok(ApiResponse.paginatedSuccess("Guides fetched successfully", result));
        }

        // Default: Full list Flow (Backward compatibility)
        List<SnapGuide> guides;
        if (starred != null && starred) {
            guides = guideService.getStarredGuides(user, orgId);
        } else if (search != null && !search.trim().isEmpty()) {
            guides = guideService.searchGuides(user, orgId, search);
        } else {
            guides = guideService.getGuidesForUser(user, orgId);
        }
        return ResponseEntity.ok(ApiResponse.success("Guides fetched successfully", guides));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/guides/{externalId}")
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

    @PutMapping("/guides")
    public ResponseEntity<ApiResponse<SnapGuide>> updateGuide(
            HttpServletRequest request,
            @RequestParam(required = false) Long id,
            @RequestBody SnapGuideDto guideDto) {

        // Use ID from body if query param is null
        Long targetId = (id != null) ? id : guideDto.getId();

        if (targetId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing guide ID in query or body"));
        }

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        // Use Org ID from body or cookie
        Long orgId = guideDto.getOrgId();
        if (orgId == null) {
            orgId = getOrganizationId(request, user);
        }

        if (orgId == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Organization context missing"));
        }

        try {
            SnapGuide updatedGuide = guideService.updateGuide(targetId, guideDto, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Guide updated successfully", updatedGuide));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update guide: " + e.getMessage()));
        }
    }

    @DeleteMapping("/guides")
    public ResponseEntity<ApiResponse<Void>> deleteGuideByParam(
            HttpServletRequest request,
            @RequestParam(required = false) Long id) {

        if (id == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing required parameter: id"));
        }

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Long orgId = getOrganizationId(request, user);
        if (orgId == null) {
            return ResponseEntity.status(400).body(ApiResponse.error("Organization context missing"));
        }

        try {
            guideService.deleteGuideByNumericId(id, user, orgId);
            return ResponseEntity.ok(ApiResponse.success("Guide deleted successfully", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/guides/{externalId}")
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

    @GetMapping("/guides/export")
    public ResponseEntity<byte[]> exportGuide(
            HttpServletRequest request,
            @RequestParam Long id,
            @RequestParam String type) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Long orgId = getOrganizationId(request, user);
        if (orgId == null) {
            return ResponseEntity.status(400).build();
        }

        try {
            byte[] data = guideService.exportGuide(id, type, user, orgId);

            String extension;
            if (type.equalsIgnoreCase("HTML"))
                extension = ".html";
            else if (type.equalsIgnoreCase("PDF"))
                extension = ".pdf";
            else
                extension = ".md";

            String fileName = "guide_" + id + extension;

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            System.err.println("EXPORT ERROR for type=" + type + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/support/tickets")
    public ResponseEntity<ApiResponse<SnapTicket>> createTicket(
            HttpServletRequest request,
            @RequestBody com.nrkgo.accounts.modules.snapsteps.dto.SnapTicketDto ticketDto) {

        User user = getAuthenticatedUser(request);
        Long orgId = null;
        if (user != null) {
            orgId = getOrganizationId(request, user);
        }

        try {
            SnapTicket ticket = ticketService.createTicket(ticketDto, orgId);
            return ResponseEntity.ok(ApiResponse.success("Ticket submitted successfully", ticket));
        } catch (Exception e) {
            System.err.println("SUPPORT SUBMISSION ERROR: [" + e.getClass().getName() + "] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to submit ticket: " + e.getMessage()));
        }
    }
}

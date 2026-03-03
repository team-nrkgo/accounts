package com.nrkgo.accounts.modules.snapsteps.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import com.nrkgo.accounts.modules.snapsteps.service.SnapGuideService;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guides")
public class SnapGuideController {

    private final SnapGuideService guideService;
    private final UserService userService;

    public SnapGuideController(SnapGuideService guideService, UserService userService) {
        this.guideService = guideService;
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

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<SnapGuide>> saveGuide(
            HttpServletRequest request,
            @RequestBody SnapGuideDto guideDto) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            SnapGuide savedGuide = guideService.saveGuide(guideDto, user);
            return ResponseEntity.ok(ApiResponse.success("Guide saved successfully", savedGuide));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to save guide: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SnapGuide>>> listGuides(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        List<SnapGuide> guides = guideService.getGuidesForUser(user);
        return ResponseEntity.ok(ApiResponse.success("Guides fetched successfully", guides));
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<ApiResponse<SnapGuide>> getGuide(
            HttpServletRequest request,
            @PathVariable String externalId) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            SnapGuide guide = guideService.getGuideById(externalId, user);
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

        try {
            guideService.deleteGuide(externalId, user);
            return ResponseEntity.ok(ApiResponse.success("Guide deleted successfully", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}

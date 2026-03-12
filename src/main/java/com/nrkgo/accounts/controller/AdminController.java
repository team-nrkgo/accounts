package com.nrkgo.accounts.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.dto.UserIdRequest;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.admin.AdminGuard;
import com.nrkgo.accounts.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AdminGuard adminGuard;

    public AdminController(UserService userService, AdminGuard adminGuard) {
        this.userService = userService;
        this.adminGuard = adminGuard;
    }

    /**
     * Activate a user manually.
     * Use case: User unable to receive verification email or login.
     * 
     * @param request Contains the userId to activate
     * @return Success or error response
     */
    @PostMapping("/users/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@RequestBody UserIdRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            User requester = getAuthenticatedUser(httpRequest);
            if (requester == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
            }

            try {
                adminGuard.requireAdmin(requester);
            } catch (SecurityException e) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Unauthorized: Product Owner access required"));
            }

            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID is required"));
            }
            userService.activateUser(request.getUserId());
            return ResponseEntity.ok(ApiResponse.success("User activated successfully",
                    "User with ID " + request.getUserId() + " is now active."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private User getAuthenticatedUser(jakarta.servlet.http.HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
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
}

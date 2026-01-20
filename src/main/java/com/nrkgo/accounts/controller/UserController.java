package com.nrkgo.accounts.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.dto.UpdateUserRequest;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
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
        if (token == null) return null;
        return userService.getUserBySession(token);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getUserProfile(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success("User profile fetched", user));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateUserProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest updateRequest) {
        
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        User updatedUser = userService.updateUser(user.getId(), updateRequest);
        return ResponseEntity.ok(ApiResponse.success("User profile updated", updatedUser));
    }
}

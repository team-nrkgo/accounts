package com.nrkgo.accounts.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.dto.LoginRequest;
import com.nrkgo.accounts.dto.SignupRequest;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.model.UserSession;
import com.nrkgo.accounts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    // Manual Constructor for Dependency Injection
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<User>> signup(@Valid @RequestBody SignupRequest request, 
                                                  jakarta.servlet.http.HttpServletRequest httpRequest,
                                                  jakarta.servlet.http.HttpServletResponse response) {
        User user = userService.registerUser(request);
        
        // Auto-Login: Create Session
        UserSession session = userService.createSession(user, httpRequest);
        setCookie(response, session.getCookie());

        // In production, do not return password hash
        user.setPassword(null); 
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserSession>> login(@Valid @RequestBody LoginRequest request, 
                                                       jakarta.servlet.http.HttpServletRequest httpRequest,
                                                       jakarta.servlet.http.HttpServletResponse response) {
        try {
            UserSession session = userService.loginUser(request, httpRequest);
            setCookie(response, session.getCookie());
            return ResponseEntity.ok(ApiResponse.success("Login successful", session));
        } catch (com.nrkgo.accounts.exception.UserNotVerifiedException e) {
            // Resend Email in a wrapper transaction (handled by service)
            userService.resendVerificationEmail(request.getEmail());
            return ResponseEntity.badRequest().body(ApiResponse.error("Email not verified. A new verification email has been sent to " + request.getEmail()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("user_session", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/check-status")
    public ResponseEntity<ApiResponse<Integer>> checkStatus(@RequestParam String email) {
        Integer status = userService.checkUserStatus(email);
        if (status == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("User status retrieved", status));
    }

    @GetMapping("/ustatus")
    public ResponseEntity<ApiResponse<Boolean>> validateSession(jakarta.servlet.http.HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("user_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        if (token == null) {
            return ResponseEntity.ok(ApiResponse.success("No session cookie found", false));
        }

        boolean isValid = userService.validateSession(token);
        return ResponseEntity.ok(ApiResponse.success("Session validation result", isValid));
    }
    
    @GetMapping("/init")
    public ResponseEntity<ApiResponse<com.nrkgo.accounts.dto.InitResponse>> init(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestParam(name = "org_id", required = false) Long orgId) {
        
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("user_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        if (token == null) {
             return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated: No session found"));
        }
        
        User user = userService.getUserBySession(token);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated: Invalid or expired session"));
        }
        
        com.nrkgo.accounts.dto.InitResponse response = userService.getInitData(user.getId(), orgId);
        return ResponseEntity.ok(ApiResponse.success("Initialization data fetched", response));
    }

    @GetMapping("/verify")
    public void verifyUser(@RequestParam String token, 
                           jakarta.servlet.http.HttpServletRequest request,
                           jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        try {
            User user = userService.verifyUser(token);
            if (user != null) {
                // Auto-Login: Create Session
                UserSession session = userService.createSession(user, request);
                setCookie(response, session.getCookie());
                
                // Redirect to Product Page (Homepage)
                response.sendRedirect("http://localhost:5173/");
            } else {
                response.sendRedirect("http://localhost:5173/login?error=verification_failed");
            }
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/login?error=verification_failed");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody com.nrkgo.accounts.dto.ForgotPasswordRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If an account exists with this email, a password reset link has been sent.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody com.nrkgo.accounts.dto.ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully. Please login with your new password.", null));
    }

    private void setCookie(jakarta.servlet.http.HttpServletResponse response, String token) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("user_session", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in Production (requires HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 Day
        response.addCookie(cookie);
    }
}

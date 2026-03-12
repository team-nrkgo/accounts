package com.nrkgo.accounts.modules.integrations.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.integrations.service.OAuthProvider;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/integrations/oauth")
public class IntegrationOAuthController {

    private final Map<String, OAuthProvider> providers;
    private final UserService userService;

    public IntegrationOAuthController(List<OAuthProvider> providerList, UserService userService) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(OAuthProvider::getProviderName, Function.identity()));
        this.userService = userService;
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

    /**
     * Generic Authorize Endpoint
     * GET
     * /api/integrations/oauth/authorize?type=wordpress&org_id=1&product_code=2&redirect_uri=...
     */
    @GetMapping("/authorize")
    public ResponseEntity<ApiResponse<String>> getAuthorizeUrl(
            HttpServletRequest request,
            @RequestParam String type,
            @RequestParam(name = "org_id") Long orgId,
            @RequestParam(name = "product_code") Integer productCode,
            @RequestParam(name = "redirect_uri") String redirectUri) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        OAuthProvider provider = providers.get(type.toLowerCase());
        if (provider == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unsupported integration type: " + type));
        }

        try {
            String url = provider.getAuthorizeUrl(user.getId(), orgId, productCode, redirectUri);
            return ResponseEntity.ok(ApiResponse.success("Authorize URL generated for " + type, url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Generic Callback Endpoint
     * GET
     * /api/integrations/oauth/callback?type=wordpress&code=...&state=...&redirect_uri=...
     */
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<String>> callback(
            @RequestParam String type,
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(name = "redirect_uri") String redirectUri) {

        OAuthProvider provider = providers.get(type.toLowerCase());
        if (provider == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unsupported integration type: " + type));
        }

        try {
            provider.handleCallback(code, state, redirectUri);
            return ResponseEntity.ok(ApiResponse.success(type + " connected successfully", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Callback failed: " + e.getMessage()));
        }
    }
}

package com.nrkgo.accounts.modules.integrations.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.integrations.model.ExternalAccount;
import com.nrkgo.accounts.modules.integrations.model.UserExternalCreds;
import com.nrkgo.accounts.modules.integrations.repository.UserExternalCredsRepository;
import com.nrkgo.accounts.modules.integrations.service.IntegrationServiceImpl;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationServiceImpl integrationService;
    private final UserService userService;
    private final UserExternalCredsRepository credsRepository;
    private final com.nrkgo.accounts.modules.integrations.repository.OrgIntegrationRepository orgIntegrationRepository;

    public IntegrationController(IntegrationServiceImpl integrationService,
            UserService userService,
            UserExternalCredsRepository credsRepository,
            com.nrkgo.accounts.modules.integrations.repository.OrgIntegrationRepository orgIntegrationRepository) {
        this.integrationService = integrationService;
        this.userService = userService;
        this.credsRepository = credsRepository;
        this.orgIntegrationRepository = orgIntegrationRepository;
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

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<ExternalAccount>> connect(
            HttpServletRequest request,
            @RequestBody Map<String, Object> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        String provider = (String) payload.get("provider");
        String providerAccountId = (String) payload.get("provider_account_id");
        String displayName = (String) payload.get("display_name");
        String apiKey = (String) payload.get("api_key");

        // Fallback for API Key based integrations where provider_account_id isn't known
        // yet
        if (providerAccountId == null || providerAccountId.isBlank()) {
            providerAccountId = "default";
        }

        if (provider == null || apiKey == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Provider and API Key are required"));
        }

        try {
            // Determine AuthType
            com.nrkgo.accounts.modules.integrations.model.AuthType authType = (apiKey != null && !apiKey.isBlank())
                    ? com.nrkgo.accounts.modules.integrations.model.AuthType.API_KEY
                    : com.nrkgo.accounts.modules.integrations.model.AuthType.OAUTH;

            // 1. IDENTITY & VAULT: Always save to the master secure vault first
            ExternalAccount account = integrationService.connectSocialAccount(
                    user.getId(), provider, providerAccountId, null, displayName, null,
                    apiKey, null, null, "full_access", authType);

            // 2. AUTO-ACTIVATION: If Org/Product context is provided, link it instantly
            if (payload.containsKey("org_id") && payload.containsKey("product_code")) {
                Long orgId = Long.valueOf(payload.get("org_id").toString());
                Integer productCode = Integer.valueOf(payload.get("product_code").toString());
                String configJson = payload.get("config_json") != null ? payload.get("config_json").toString() : "{}";

                integrationService.activateOrgIntegration(orgId, productCode, account.getId(), configJson);
            }

            return ResponseEntity.ok(ApiResponse.success(provider + " connected and ready", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to connect: " + e.getMessage()));
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyIntegrations(
            HttpServletRequest request,
            @RequestParam(name = "org_id", required = false) Long orgId,
            @RequestParam(name = "product_code", required = false) Integer productCode) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        // Validation: If checking status for an Org, product_code is MANDATORY
        if (orgId != null && productCode == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("product_code is required when org_id is provided"));
        }

        List<UserExternalCreds> creds = credsRepository.findByUserId(user.getId());

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (UserExternalCreds c : creds) {
            ExternalAccount acc = c.getExternalAccount();
            if (acc == null)
                continue;

            Map<String, Object> map = new java.util.HashMap<>();
            map.put("connection_id", acc.getId()); // using snake_case for UI consistency
            map.put("provider", acc.getProvider());
            map.put("display_name", acc.getDisplayName() != null ? acc.getDisplayName() : acc.getProvider());
            map.put("email", acc.getEmail() != null ? acc.getEmail() : "");
            map.put("avatar_url", acc.getAvatarUrl() != null ? acc.getAvatarUrl() : "");
            map.put("auth_type", c.getAuthType());

            // Connection Expiry Info
            map.put("expiry_time", c.getExpiryTime());
            boolean isExpired = false;
            if (c.getExpiryTime() != null && c.getExpiryTime() > 0) {
                isExpired = System.currentTimeMillis() > c.getExpiryTime();
            }
            map.put("is_expired", isExpired);

            // Status Check: Is this connection active for THIS org/product?
            boolean isActive = false;
            if (orgId != null && productCode != null) {
                isActive = orgIntegrationRepository
                        .findByOrgIdAndProductCodeAndExternalAccount(orgId, productCode, acc)
                        .map(i -> i.getStatus() == 1)
                        .orElse(false);
            }
            map.put("is_active", isActive);

            result.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Integrations fetched", result));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activateIntegration(
            HttpServletRequest request,
            @RequestBody Map<String, Object> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            Object orgIdObj = payload.getOrDefault("org_id", payload.get("orgId"));
            Object productCodeObj = payload.getOrDefault("product_code", payload.get("productCode"));
            Object connectionIdObj = payload.getOrDefault("connection_id", payload.get("connectionId"));

            if (orgIdObj == null || productCodeObj == null || connectionIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing required fields: org_id, product_code, connection_id"));
            }

            Long orgId = Long.valueOf(orgIdObj.toString());
            Integer productCode = Integer.valueOf(productCodeObj.toString());
            Long connectionId = Long.valueOf(connectionIdObj.toString());
            String configJson = payload.get("config_json") != null ? payload.get("config_json").toString()
                    : (payload.get("configJson") != null ? payload.get("configJson").toString() : "{}");

            integrationService.activateOrgIntegration(orgId, productCode, connectionId, configJson);
            return ResponseEntity.ok(ApiResponse.success("Integration activated for organization", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Activation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateIntegration(
            HttpServletRequest request,
            @RequestBody Map<String, Object> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            Object orgIdObj = payload.getOrDefault("org_id", payload.get("orgId"));
            Object productCodeObj = payload.getOrDefault("product_code", payload.get("productCode"));
            Object connectionIdObj = payload.getOrDefault("connection_id", payload.get("connectionId"));

            if (orgIdObj == null || productCodeObj == null || connectionIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing required fields: org_id, product_code, connection_id"));
            }

            Long orgId = Long.valueOf(orgIdObj.toString());
            Integer productCode = Integer.valueOf(productCodeObj.toString());
            Long connectionId = Long.valueOf(connectionIdObj.toString());

            integrationService.deactivateOrgIntegration(orgId, productCode, connectionId);
            return ResponseEntity.ok(ApiResponse.success("Integration disconnected from organization", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Deactivation failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/disconnect/{connectionId}")
    public ResponseEntity<ApiResponse<Void>> disconnectAccount(
            HttpServletRequest request,
            @PathVariable Long connectionId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        try {
            integrationService.disconnectSocialAccount(user.getId(), connectionId);
            return ResponseEntity.ok(ApiResponse.success("Account disconnected completely", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Disconnect failed: " + e.getMessage()));
        }
    }
}

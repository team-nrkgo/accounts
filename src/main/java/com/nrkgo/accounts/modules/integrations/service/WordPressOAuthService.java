package com.nrkgo.accounts.modules.integrations.service;

import com.nrkgo.accounts.modules.integrations.model.AuthType;
import com.nrkgo.accounts.modules.integrations.model.ExternalAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class WordPressOAuthService implements OAuthProvider {

    @Override
    public String getProviderName() {
        return "wordpress";
    }

    @Value("${wordpress.oauth.client-id:}")
    private String clientId;

    @Value("${wordpress.oauth.client-secret:}")
    private String clientSecret;

    @Value("${wordpress.oauth.authorize-url:https://public-api.wordpress.com/oauth2/authorize}")
    private String authorizeUrl;

    @Value("${wordpress.oauth.token-url:https://public-api.wordpress.com/oauth2/token}")
    private String tokenUrl;

    private final IntegrationServiceImpl integrationService;
    private final RestTemplate restTemplate;

    public WordPressOAuthService(IntegrationServiceImpl integrationService) {
        this.integrationService = integrationService;
        this.restTemplate = new RestTemplate();
    }

    public String getAuthorizeUrl(Long userId, Long orgId, Integer productCode, String redirectUri) {
        // State is used to capture context after redirect
        String state = String.format("%d:%d:%d", userId, orgId, productCode);

        return UriComponentsBuilder.fromHttpUrl(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "auth")
                .queryParam("state", state)
                .build().toUriString();
    }

    public void handleCallback(String code, String state, String redirectUri) {
        // 1. Parse state
        String[] parts = state.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid state parameter");
        }

        Long userId = Long.valueOf(parts[0]);
        Long orgId = Long.valueOf(parts[1]);
        Integer productCode = Long.valueOf(parts[2]).intValue();

        // 2. Exchange code for token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");
            String blogId = responseBody.get("blog_id") != null ? responseBody.get("blog_id").toString() : "0";
            String blogUrl = (String) responseBody.get("blog_url");

            // 3. Store in Vault
            // Provider account ID for WP.com is often the blog_id
            ExternalAccount account = integrationService.connectSocialAccount(
                    userId, "wordpress", blogId, null, blogUrl, null,
                    accessToken, null, null, "auth", AuthType.OAUTH);

            // 4. Activate for this Org + Product
            // Pass the blog_url in config for later use
            String configJson = String.format("{\"siteUrl\": \"%s\", \"blogId\": \"%s\"}", blogUrl, blogId);
            integrationService.activateOrgIntegration(orgId, productCode, account.getId(), configJson);
        } else {
            throw new RuntimeException("Failed to exchange code for token: " + response.getStatusCode());
        }
    }
}

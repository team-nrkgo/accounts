package com.nrkgo.accounts.modules.echo.controller;

import com.nrkgo.accounts.common.ai.GeminiAiService;
import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.service.EchoAiConfigService;
import com.nrkgo.accounts.modules.echo.service.EchoResearchService;
import com.nrkgo.accounts.modules.echo.service.EchoUrlContextService;
import com.nrkgo.accounts.modules.echo.dto.EchoResearchDetailDto;
import com.nrkgo.accounts.modules.echo.model.EchoPaaQuestion;
import com.nrkgo.accounts.modules.integrations.service.IntegrationServiceImpl;
import com.nrkgo.accounts.service.UserService;
import com.nrkgo.accounts.repository.OrgUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/echo/ai")
public class EchoAiController {

    private final EchoAiConfigService configService;
    private final GeminiAiService geminiAiService;
    private final UserService userService;
    private final OrgUserRepository orgUserRepository;
    private final EchoResearchService researchService;
    private final EchoUrlContextService urlContextService;
    private final IntegrationServiceImpl integrationService;

    public EchoAiController(EchoAiConfigService configService, GeminiAiService geminiAiService,
            UserService userService, OrgUserRepository orgUserRepository,
            EchoResearchService researchService, EchoUrlContextService urlContextService,
            IntegrationServiceImpl integrationService) {
        this.configService = configService;
        this.geminiAiService = geminiAiService;
        this.userService = userService;
        this.orgUserRepository = orgUserRepository;
        this.researchService = researchService;
        this.urlContextService = urlContextService;
        this.integrationService = integrationService;
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

    private void validateOrganizationMembership(User user, Long orgId) {
        if (orgId == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        boolean isMember = orgUserRepository.existsByOrgIdAndUserId(orgId, user.getId());
        if (!isMember) {
            throw new SecurityException("Access Denied: You are not a member of this organization");
        }
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAiConfig(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);
            Map<String, String> config = new HashMap<>();
            config.put("blogPrompt", configService.getBlogPrompt(orgId));
            config.put("model", configService.getAiModel(orgId));

            return ResponseEntity.ok(ApiResponse.success("AI config fetched", config));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Void>> updateAiConfig(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestBody Map<String, String> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);

            if (payload.containsKey("blogPrompt")) {
                configService.setBlogPrompt(orgId, payload.get("blogPrompt"));
            }
            if (payload.containsKey("model")) {
                configService.setAiModel(orgId, payload.get("model"));
            }

            return ResponseEntity.ok(ApiResponse.success("AI config updated", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/generate-blog")
    public ResponseEntity<ApiResponse<String>> generateBlog(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestBody Map<String, String> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);

            String topic = payload.get("topic");
            if (topic == null || topic.isEmpty()) {
                throw new IllegalArgumentException("Topic is required to generate a blog");
            }

            // Fetch custom AI settings from DB
            String systemPrompt = configService.getBlogPrompt(orgId);
            String customModel = configService.getAiModel(orgId);

            // Fetch URL Context
            String url = payload.get("url");
            String extractedContext = "";
            if (url != null && !url.trim().isEmpty()) {
                try {
                    extractedContext = urlContextService.fetchAndConvertToMarkdown(url.trim());
                } catch (Exception ex) {
                    System.err.println("Failed to fetch context from URL: " + url + " - " + ex.getMessage());
                }
            }

            // Build the user payload mapping the topic (we can also include PAA questions
            // if they passed it)
            String userPrompt = "Please write a comprehensive blog post about: " + topic;

            if (payload.containsKey("context_data")) {
                userPrompt += "\n\nUse this research context data:\n" + payload.get("context_data");
            }

            if (!extractedContext.isEmpty()) {
                userPrompt += "\n\nContent extracted from URL (" + url + "):\n" + extractedContext;
            }

            // Enforce Integration Connection: Token must be active for Echo (Product Code
            // 2)
            String customApiKey = integrationService.getActiveIntegrationCredential(orgId, 2, "gemini");

            // Call the reusable Gemini API client with Auditing
            String generatedContent = geminiAiService.generateContentWithAudit(userPrompt, systemPrompt, customModel,
                    customApiKey, orgId, user.getId(), "ECHO", "GENERATE_BLOG");

            return ResponseEntity.ok(ApiResponse.success("Blog generated successfully", generatedContent));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/playground")
    public ResponseEntity<ApiResponse<String>> testPromptPlayground(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestBody Map<String, String> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);

            String topic = payload.get("topic");
            if (topic == null || topic.isEmpty()) {
                throw new IllegalArgumentException("Topic is required to run playground");
            }

            // Fetch URL Context
            String url = payload.get("url");
            String extractedContext = "";
            if (url != null && !url.trim().isEmpty()) {
                try {
                    extractedContext = urlContextService.fetchAndConvertToMarkdown(url.trim());
                } catch (Exception ex) {
                    System.err.println("Failed to fetch context from URL: " + url + " - " + ex.getMessage());
                }
            }

            // If user provided a test prompt, use it. Otherwise, fallback to settings.
            String systemPrompt = payload.containsKey("prompt_test")
                    ? payload.get("prompt_test")
                    : configService.getBlogPrompt(orgId);

            String customModel = configService.getAiModel(orgId);

            String userPrompt = "Please write a comprehensive blog post about: " + topic;
            if (payload.containsKey("context_data")) {
                userPrompt += "\n\nUse this research context data:\n" + payload.get("context_data");
            }

            if (!extractedContext.isEmpty()) {
                userPrompt += "\n\nContent extracted from URL (" + url + "):\n" + extractedContext;
            }

            // Enforce Integration Connection
            String customApiKey = integrationService.getActiveIntegrationCredential(orgId, 2, "gemini");

            String generatedContent = geminiAiService.generateContentWithAudit(userPrompt, systemPrompt, customModel,
                    customApiKey, orgId, user.getId(), "ECHO", "PLAYGROUND_TEST");
            return ResponseEntity.ok(ApiResponse.success("Playground blog generated successfully", generatedContent));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/generate-from-search")
    public ResponseEntity<ApiResponse<String>> generateFromSearch(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestBody Map<String, Object> payload) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);

            if (!payload.containsKey("search_id")) {
                throw new IllegalArgumentException("search_id is required");
            }

            Long searchId = Long.valueOf(payload.get("search_id").toString());

            // Get research data
            EchoResearchDetailDto researchDetails = researchService.getSearchResultDetails(searchId, orgId);

            String topic = researchDetails.getSearch().getKeyword();

            // Build Context Data
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Keyword: ").append(topic).append("\n");

            if (researchDetails.getSearch().getSourceUrl() != null) {
                contextBuilder.append("Primary Source URL: ").append(researchDetails.getSearch().getSourceUrl())
                        .append("\n");
            }

            if (researchDetails.getQuestions() != null && !researchDetails.getQuestions().isEmpty()) {
                contextBuilder.append("\nQuestions (People Also Ask):\n");
                for (EchoPaaQuestion q : researchDetails.getQuestions()) {
                    contextBuilder.append("- ").append(q.getQuestion());
                    if (q.getAnswerUrl() != null) {
                        contextBuilder.append(" (Source: ").append(q.getAnswerUrl()).append(")");
                    }
                    contextBuilder.append("\n");
                }
            }

            String systemPrompt = configService.getBlogPrompt(orgId);
            String customModel = configService.getAiModel(orgId);

            String userPrompt = "Please write a comprehensive blog post about: " + topic;
            userPrompt += "\n\nUse this detailed research context data:\n" + contextBuilder.toString();

            // Enforce Integration Connection
            String customApiKey = integrationService.getActiveIntegrationCredential(orgId, 2, "gemini");

            String generatedContent = geminiAiService.generateContentWithAudit(userPrompt, systemPrompt, customModel,
                    customApiKey, orgId, user.getId(), "ECHO", "GENERATE_FROM_SEARCH");
            return ResponseEntity.ok(ApiResponse.success("Blog generated from search successfully", generatedContent));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

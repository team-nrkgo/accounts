package com.nrkgo.accounts.common.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    private final String defaultApiKey;
    private final String baseUrl;
    private final String defaultModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiAuditLogRepository auditLogRepository;

    public GeminiAiService(
            @Value("${gemini.api.key:}") String defaultApiKey,
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}") String baseUrl,
            @Value("${gemini.default.model:gemini-2.5-flash}") String defaultModel,
            AiAuditLogRepository auditLogRepository) {
        this.defaultApiKey = defaultApiKey;
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Common reusable flow to call Gemini AI with a specific prompt.
     *
     * @param prompt            The main user prompt.
     * @param systemInstruction The system instructions (e.g., "You are a
     *                          copywriter"). Can be null.
     * @param model             The model override (e.g., "gemini-1.5-pro"). Null to
     *                          use default.
     * @param customApiKey      The API key override. Null to use
     *                          application.properties default.
     * @return The AI generated text response.
     */
    public String generateContent(String prompt, String systemInstruction, String model, String customApiKey) {
        return generateContentWithAudit(prompt, systemInstruction, model, customApiKey, null, null, "SYSTEM",
                "UNAUDITED");
    }

    /**
     * Audited reusable flow to call Gemini AI and record token and timing usage
     * globally.
     */
    public String generateContentWithAudit(String prompt, String systemInstruction, String model, String customApiKey,
            Long orgId, Long userId, String productModule, String operationType) {
        String useModel = (model != null && !model.isEmpty()) ? model : defaultModel;
        String useApiKey = (customApiKey != null && !customApiKey.isEmpty()) ? customApiKey : defaultApiKey;

        if (useApiKey == null || useApiKey.isEmpty()) {
            throw new IllegalArgumentException("Gemini API key is missing. Set GEMINI_API_KEY environment variable.");
        }

        String url = String.format("%s/%s:generateContent?key=%s", baseUrl, useModel, useApiKey);

        long startTime = System.currentTimeMillis();
        AiAuditLog audit = new AiAuditLog();
        audit.setOrgId(orgId != null ? orgId : 0L);
        audit.setUserId(userId);
        audit.setProductModule(productModule != null ? productModule : "SYSTEM");
        audit.setModelProvider("GEMINI");
        audit.setModelName(useModel);
        audit.setOperationType(operationType);
        audit.setCreatedTime(startTime);

        try {
            // Build the dynamic JSON payload for Gemini REST API
            ObjectNode requestBody = objectMapper.createObjectNode();

            // Setup contents (user message)
            ArrayNode contentsArr = requestBody.putArray("contents");
            ObjectNode contentObj = contentsArr.addObject();
            contentObj.put("role", "user");
            ArrayNode partsArr = contentObj.putArray("parts");
            partsArr.addObject().put("text", prompt);

            // Setup system instructions if provided
            if (systemInstruction != null && !systemInstruction.isEmpty()) {
                ObjectNode sysInstructObj = requestBody.putObject("systemInstruction");
                sysInstructObj.put("role", "system");
                ArrayNode sysPartsArr = sysInstructObj.putArray("parts");
                sysPartsArr.addObject().put("text", systemInstruction);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            long executionTime = System.currentTimeMillis() - startTime;
            audit.setExecutionTimeMs(executionTime);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Try to extract usage metadata
                JsonNode usageNode = rootNode.path("usageMetadata");
                if (!usageNode.isMissingNode()) {
                    audit.setInputTokens(usageNode.path("promptTokenCount").asInt(0));
                    audit.setOutputTokens(usageNode.path("candidatesTokenCount").asInt(0));
                    audit.setTotalTokens(usageNode.path("totalTokenCount").asInt(0));
                }

                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode firstCandidate = candidates.get(0);
                    JsonNode parts = firstCandidate.path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        auditLogRepository.save(audit);
                        return parts.get(0).path("text").asText();
                    }
                }
            }

            audit.setStatus("FAILED");
            audit.setErrorMessage("Unexpected response: " + response.getBody());
            auditLogRepository.save(audit);
            throw new RuntimeException("Unexpected response from Gemini API: " + response.getBody());

        } catch (HttpStatusCodeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            audit.setExecutionTimeMs(executionTime);
            audit.setStatus("FAILED");
            audit.setErrorMessage(e.getResponseBodyAsString());
            auditLogRepository.save(audit);
            log.error("HTTP Error from Gemini API: Status={}, ResponseBody={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Gemini API Error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            audit.setExecutionTimeMs(executionTime);
            audit.setStatus("FAILED");
            audit.setErrorMessage(e.getMessage());
            auditLogRepository.save(audit);
            log.error("Failed to generate content with Gemini API", e);
            throw new RuntimeException("Failed to generate content with Gemini API", e);
        }
    }

    /**
     * Overloaded method for the simplest use case (defaults only).
     */
    public String generateContent(String prompt) {
        return generateContent(prompt, null, null, null);
    }
}

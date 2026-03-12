package com.nrkgo.accounts.modules.echo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class WordPressService {

    private static final Logger logger = LoggerFactory.getLogger(WordPressService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WordPressService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String publishPost(String siteUrl, String username, String appPassword, EchoPostDto postDto) {
        try {
            String apiUrl = siteUrl.replaceAll("/$", "") + "/wp-json/wp/v2/posts";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (username != null && !username.isEmpty()) {
                String auth = username + ":" + appPassword;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            } else {
                // OAuth2 Bearer Auth
                headers.set("Authorization", "Bearer " + appPassword);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("title", postDto.getTitle());

            // For content, we extract plain text from Tiptap JSON for now
            String content = extractTextFromJson(postDto.getContentJson());
            body.put("content", content);
            body.put("status", "publish");
            body.put("slug", postDto.getSlug());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Map responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("id")) {
                    return responseBody.get("id").toString();
                }
            }

            logger.error("WordPress publish failed with status: {}", response.getStatusCode());
            return null;

        } catch (Exception e) {
            logger.error("Error publishing to WordPress: ", e);
            return null;
        }
    }

    private String extractTextFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return extractContent(map);
        } catch (Exception e) {
            return json; // Fallback to raw JSON if parsing fails
        }
    }

    private String extractContent(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder();

            if (map.containsKey("text")) {
                sb.append(map.get("text").toString());
            }

            if (map.containsKey("content") && map.get("content") instanceof java.util.List) {
                java.util.List<Object> content = (java.util.List<Object>) map.get("content");
                for (Object child : content) {
                    sb.append(extractContent(child));
                }
            }

            if ("paragraph".equals(map.get("type"))) {
                sb.append("\n\n");
            }

            return sb.toString();
        }
        return "";
    }
}

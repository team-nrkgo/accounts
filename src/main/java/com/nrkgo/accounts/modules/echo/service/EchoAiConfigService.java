package com.nrkgo.accounts.modules.echo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nrkgo.accounts.modules.echo.model.EchoSettings;
import com.nrkgo.accounts.modules.echo.repository.EchoSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EchoAiConfigService {

    private final EchoSettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    // The default system prompt if the user hasn't altered it.
    private static final String DEFAULT_BLOG_PROMPT = "You are an expert SEO blog writer. Write a highly engaging, SEO-optimized blog "
            +
            "based on the provided keyword and topic. Use clear headings, bullet points, " +
            "and short paragraphs. Ensure human-like readability.";

    public EchoAiConfigService(EchoSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets the custom blog prompt from the organization's settings.
     * If unaltered, returns the default.
     */
    public String getBlogPrompt(Long orgId) {
        return getAiConfigValue(orgId, "blogPrompt", DEFAULT_BLOG_PROMPT);
    }

    /**
     * Updates the custom blog prompt in the organization's settings.
     */
    public void setBlogPrompt(Long orgId, String customPrompt) {
        setAiConfigValue(orgId, "blogPrompt", customPrompt);
    }

    /**
     * Gets the custom AI model (e.g., gemini-1.5-pro) or null if not set.
     */
    public String getAiModel(Long orgId) {
        return getAiConfigValue(orgId, "model", null);
    }

    public void setAiModel(Long orgId, String modelName) {
        setAiConfigValue(orgId, "model", modelName);
    }

    private String getAiConfigValue(Long orgId, String key, String defaultValue) {
        Optional<EchoSettings> optSettings = settingsRepository.findByOrgId(orgId);
        if (optSettings.isEmpty())
            return defaultValue;

        String metaJson = optSettings.get().getMetaJson();
        if (metaJson == null || metaJson.trim().isEmpty())
            return defaultValue;

        try {
            JsonNode root = objectMapper.readTree(metaJson);
            JsonNode aiConfig = root.path("aiConfig");
            if (aiConfig.isObject() && aiConfig.hasNonNull(key)) {
                return aiConfig.get(key).asText();
            }
        } catch (JsonProcessingException e) {
            // Unparseable JSON, fall back to default
        }
        return defaultValue;
    }

    private void setAiConfigValue(Long orgId, String key, String value) {
        EchoSettings settings = settingsRepository.findByOrgId(orgId).orElse(new EchoSettings());
        settings.setOrgId(orgId);

        String metaJson = settings.getMetaJson();
        ObjectNode root;
        try {
            if (metaJson == null || metaJson.trim().isEmpty()) {
                root = objectMapper.createObjectNode();
            } else {
                root = (ObjectNode) objectMapper.readTree(metaJson);
            }

            ObjectNode aiConfig = (ObjectNode) root.path("aiConfig");
            if (aiConfig.isMissingNode() || !aiConfig.isObject()) {
                aiConfig = root.putObject("aiConfig");
            }

            if (value == null) {
                aiConfig.remove(key);
            } else {
                aiConfig.put(key, value);
            }

            settings.setMetaJson(objectMapper.writeValueAsString(root));
            settingsRepository.save(settings);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save AI configuration to settings", e);
        }
    }
}

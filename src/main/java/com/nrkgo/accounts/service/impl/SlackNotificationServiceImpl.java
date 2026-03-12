package com.nrkgo.accounts.service.impl;

import com.nrkgo.accounts.config.SlackConfig;
import com.nrkgo.accounts.common.notification.util.SlackBlockMessageBuilder;
import com.nrkgo.accounts.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Service
public class SlackNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SlackNotificationServiceImpl.class);

    private final SlackConfig slackConfig;
    private final RestTemplate restTemplate;

    public SlackNotificationServiceImpl(SlackConfig slackConfig, RestTemplate restTemplate) {
        this.slackConfig = slackConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    @Async
    public void sendAlert(String title, String message, String level) {
        sendAlert(title, message, level, null);
    }

    @Override
    @Async
    public void sendAlert(String title, String message, String level, Map<String, Object> metadata) {
        if (!slackConfig.isEnabled())
            return;

        try {
            SlackBlockMessageBuilder builder = SlackBlockMessageBuilder.newBuilder()
                    .header(title)
                    .text(title + ": " + message)
                    .error(message)
                    .timeNowUtc();

            if (metadata != null) {
                metadata.forEach(builder::addField);
            }

            // Use specific channel for alerts if configured, else fallback to webhook
            // default
            if (slackConfig.getChannel() != null && slackConfig.getChannel().getAlerts() != null) {
                builder.channel(slackConfig.getChannel().getAlerts());
            }

            postToSlack(builder.build());
        } catch (Exception e) {
            log.error("Failed to send Slack alert", e);
        }
    }

    @Override
    @Async
    public void sendError(String title, Throwable throwable, Map<String, Object> metadata) {
        if (!slackConfig.isEnabled())
            return;

        try {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            SlackBlockMessageBuilder builder = SlackBlockMessageBuilder.newBuilder()
                    .header("🚨 " + title)
                    .text("Error: " + title)
                    .error(throwable.getMessage())
                    .stack(stackTrace)
                    .timeNowUtc();

            if (metadata != null) {
                metadata.forEach(builder::addField);
            }

            // Use specific channel for errors if configured
            if (slackConfig.getChannel() != null && slackConfig.getChannel().getErrors() != null) {
                builder.channel(slackConfig.getChannel().getErrors());
            }

            postToSlack(builder.build());
        } catch (Exception e) {
            log.error("Failed to send Slack error alert", e);
        }
    }

    private void postToSlack(Map<String, Object> payload) {
        if (slackConfig.getBotToken() == null || slackConfig.getBotToken().isBlank()) {
            log.warn("Slack Bot Token is not configured. Falling back to Webhook if available.");
            if (slackConfig.getWebhookUrl() == null || slackConfig.getWebhookUrl().isBlank()) {
                log.warn("No Slack notification method configured. Skipping.");
                return;
            }
            postViaWebhook(payload);
            return;
        }

        try {
            String token = slackConfig.getBotToken().trim();
            log.debug("Using Bot Token starting with: {}...", token.substring(0, Math.min(token.length(), 10)));

            // Trim channel ID in payload if present
            if (payload.containsKey("channel") && payload.get("channel") instanceof String) {
                payload.put("channel", ((String) payload.get("channel")).trim());
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json; charset=utf-8"); // Explicit charset
            headers.setBearerAuth(token);

            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                    payload, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://slack.com/api/chat.postMessage", entity, String.class);

            if (response.getBody() != null && response.getBody().contains("\"ok\":false")) {
                log.error("Slack API error: {}", response.getBody());
            } else {
                log.info("Slack notification sent successfully via Web API");
            }
        } catch (Exception e) {
            log.error("Error posting to Slack Web API", e);
        }
    }

    private void postViaWebhook(Map<String, Object> payload) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                    payload, headers);

            restTemplate.postForEntity(slackConfig.getWebhookUrl(), entity, String.class);
            log.info("Slack notification sent successfully via Webhook");
        } catch (Exception e) {
            log.error("Error posting to Slack Webhook", e);
        }
    }
}

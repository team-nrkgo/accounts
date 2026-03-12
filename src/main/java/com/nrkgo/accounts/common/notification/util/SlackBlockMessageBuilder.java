package com.nrkgo.accounts.common.notification.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for Slack Block Kit messages using standard Java Maps and
 * Lists.
 * Compatible with Spring's RestTemplate/Jackson serialization.
 */
public class SlackBlockMessageBuilder {

    private String channel;
    private String text = "Notification";
    private String headerText;
    private final List<Field> fields = new ArrayList<>();
    private String errorSummary;
    private String stackSnippet;
    private int stackMaxChars = 2900;
    private String timeIso;
    private String logLinkMarkdown;
    private boolean includeDividerBeforeStack = true;

    private SlackBlockMessageBuilder() {
    }

    public static SlackBlockMessageBuilder newBuilder() {
        return new SlackBlockMessageBuilder();
    }

    public SlackBlockMessageBuilder channel(String channel) {
        this.channel = channel;
        return this;
    }

    public SlackBlockMessageBuilder text(String text) {
        this.text = text;
        return this;
    }

    public SlackBlockMessageBuilder header(String headerText) {
        this.headerText = headerText;
        return this;
    }

    public SlackBlockMessageBuilder addField(String label, Object value) {
        this.fields.add(new Field(label, value == null ? "n/a" : String.valueOf(value)));
        return this;
    }

    public SlackBlockMessageBuilder error(String errorSummary) {
        this.errorSummary = errorSummary;
        return this;
    }

    public SlackBlockMessageBuilder stack(String stackSnippet) {
        this.stackSnippet = stackSnippet;
        return this;
    }

    public SlackBlockMessageBuilder timeNowUtc() {
        this.timeIso = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return this;
    }

    public SlackBlockMessageBuilder logLink(String url, String label) {
        this.logLinkMarkdown = "<" + url + "|" + label + ">";
        return this;
    }

    public Map<String, Object> build() {
        Map<String, Object> payload = new HashMap<>();
        if (channel != null) {
            payload.put("channel", channel);
        }
        payload.put("text", text);

        List<Map<String, Object>> blocks = new ArrayList<>();

        // Header
        if (headerText != null && !headerText.isBlank()) {
            blocks.add(Map.of(
                    "type", "header",
                    "text", Map.of("type", "plain_text", "text", headerText)));
        }

        // Fields Section
        if (!fields.isEmpty()) {
            List<Map<String, Object>> fieldsList = new ArrayList<>();
            for (Field f : fields) {
                String md = "*" + escape(f.label) + ":*\n" + escape(f.value);
                fieldsList.add(Map.of("type", "mrkdwn", "text", md));
            }
            blocks.add(Map.of("type", "section", "fields", fieldsList));
        }

        // Divider
        if (!fields.isEmpty()) {
            blocks.add(Map.of("type", "divider"));
        }

        // Error Summary
        if (errorSummary != null && !errorSummary.isBlank()) {
            blocks.add(Map.of(
                    "type", "section",
                    "text", Map.of("type", "mrkdwn", "text", escape(errorSummary))));
        }

        // Stack Trace
        if (stackSnippet != null && !stackSnippet.isBlank()) {
            if (includeDividerBeforeStack && errorSummary != null) {
                blocks.add(Map.of("type", "divider"));
            }
            String truncatedStack = stackSnippet.length() > stackMaxChars
                    ? stackSnippet.substring(0, stackMaxChars) + "... [truncated]"
                    : stackSnippet;

            blocks.add(Map.of(
                    "type", "section",
                    "text", Map.of("type", "mrkdwn", "text", "```" + escapeCode(truncatedStack) + "```")));
        }

        // Context: Time and Log Link
        StringBuilder ctxText = new StringBuilder();
        if (timeIso != null)
            ctxText.append("*Time (UTC):* ").append(timeIso);
        if (logLinkMarkdown != null) {
            if (ctxText.length() > 0)
                ctxText.append(" • ");
            ctxText.append(logLinkMarkdown);
        }

        if (ctxText.length() > 0) {
            blocks.add(Map.of(
                    "type", "context",
                    "elements", List.of(Map.of("type", "mrkdwn", "text", ctxText.toString()))));
        }

        payload.put("blocks", blocks);
        return payload;
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeCode(String s) {
        if (s == null)
            return "";
        return s.replace("```", "` ` `");
    }

    private static class Field {
        final String label;
        final String value;

        Field(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
}

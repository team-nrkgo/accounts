package com.nrkgo.accounts.service;

import java.util.Map;

public interface NotificationService {

    /**
     * Send a general alert message.
     */
    void sendAlert(String title, String message, String level);

    /**
     * Send a structured alert with metadata.
     */
    void sendAlert(String title, String message, String level, Map<String, Object> metadata);

    /**
     * Send an error alert with exception details.
     */
    void sendError(String title, Throwable throwable, Map<String, Object> metadata);

    /**
     * Convenience method for sending error without extra metadata.
     */
    default void sendError(String title, Throwable throwable) {
        sendError(title, throwable, null);
    }
}

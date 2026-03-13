package com.nrkgo.accounts.common.exception;

import com.nrkgo.accounts.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final com.nrkgo.accounts.service.NotificationService notificationService;

    public GlobalExceptionHandler(com.nrkgo.accounts.service.NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex,
            jakarta.servlet.http.HttpServletRequest request) {
        log.error("Unhandled exception occurred", ex);

        // Send Slack Notification
        try {
            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("Endpoint", request.getRequestURI());
            metadata.put("Method", request.getMethod());
            metadata.put("Remote IP", request.getRemoteAddr());

            notificationService.sendError("Internal Server Error", ex, metadata);
        } catch (Exception e) {
            log.error("Failed to send error notification", e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An internal error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad Request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMsg = new StringBuilder("Validation Failed: ");
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errorMsg.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; "));

        log.warn("Validation Error: {}", errorMsg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorMsg.toString().trim()));
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        // Log as info to avoid noise, as browsers often check for favicon.ico
        // automatically
        log.info("Resource not found: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found: " + ex.getResourcePath()));
    }
}

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An internal error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMsg = new StringBuilder("Validation Failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errorMsg.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; "));
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorMsg.toString().trim()));
    }
    
    // Helper to keep MethodArgumentNotValidException consistent with ApiResponse structure if needed
    // For now, let's treat validation error as a special 'error' type or just embed in data.
    // Let's refine the Validation handler to be strictly an error response.
}

package com.smms.backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler to provide meaningful error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Database constraint violation";
        
        String rootMessage = ex.getMessage();
        if (rootMessage != null) {
            if (rootMessage.contains("products") || rootMessage.contains("PRODUCTS")) {
                message = "A product with this code/name already exists in your inventory.";
            } else if (rootMessage.contains("Duplicate entry") || rootMessage.contains("Unique index or primary key violation")) {
                message = "This record already exists (Unique constraint violation).";
            } else if (rootMessage.contains("cannot be null") || rootMessage.contains("NULL not allowed")) {
                message = "Missing required field(s). Please fill all required fields.";
            }
        }
        
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = "Invalid request format";
        
        Throwable cause = ex.getCause();
        if (cause instanceof DateTimeParseException) {
            message = "Invalid date format. Please use YYYY-MM-DD format.";
        }
        
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException ex) {
        // This likely means AuthContext.getCurrentUser() returned null
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication required. Please log in again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // Log the full stack trace
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}

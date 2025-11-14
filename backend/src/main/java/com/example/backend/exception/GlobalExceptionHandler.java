package com.example.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for providing graceful error responses to the UI.
 * Handles common exceptions and converts them to user-friendly error messages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle Flowable-specific exceptions
     */
    @ExceptionHandler({
        org.flowable.common.engine.api.FlowableException.class,
        org.flowable.common.engine.api.FlowableObjectNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleFlowableException(Exception ex) {
        logger.error("Flowable exception occurred: ", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Workflow Error", 
            "An error occurred while processing the workflow: " + ex.getMessage(),
            "FLOWABLE_ERROR"
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Map<String, Object>> handleValidationException(Exception ex) {
        logger.warn("Validation error: ", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Error",
            "Invalid input provided. Please check your data and try again.",
            "VALIDATION_ERROR"
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle type conversion errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logger.warn("Type mismatch error: ", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid Parameter",
            String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                         ex.getValue(), ex.getName(), 
                         getRequiredTypeName(ex)),
            "TYPE_MISMATCH_ERROR"
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle resource not found
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception: ", ex);
        
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "INTERNAL_ERROR";
        
        // Check for specific error patterns
        if (message != null) {
            if (message.toLowerCase().contains("not found") || message.toLowerCase().contains("doesn't exist")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "RESOURCE_NOT_FOUND";
            } else if (message.toLowerCase().contains("unauthorized") || message.toLowerCase().contains("access denied")) {
                status = HttpStatus.FORBIDDEN;
                errorCode = "ACCESS_DENIED";
            } else if (message.toLowerCase().contains("invalid") || message.toLowerCase().contains("bad request")) {
                status = HttpStatus.BAD_REQUEST;
                errorCode = "INVALID_REQUEST";
            }
        }

        Map<String, Object> errorResponse = createErrorResponse(
            status,
            "Application Error",
            message != null ? message : "An unexpected error occurred",
            errorCode
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Illegal argument: ", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid Argument",
            ex.getMessage(),
            "ILLEGAL_ARGUMENT"
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        logger.error("Unexpected exception: ", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "System Error",
            "An unexpected system error occurred. Please try again later.",
            "SYSTEM_ERROR"
        );
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    /**
     * Get required type name safely
     */
    private String getRequiredTypeName(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        return requiredType != null ? requiredType.getSimpleName() : "unknown";
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String title, String message, String errorCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", title);
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("success", false);
        
        return errorResponse;
    }
}
package com.example.backend.util;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Error handling utility class for consistent exception handling across controllers
 * Provides centralized error handling patterns and response generation
 */
@UtilityClass
public class ErrorHandlingUtils {

    /**
     * Execute a service operation with standard error handling
     * @param operation the operation to execute
     * @param successMessage message for successful execution
     * @param errorMessage message for failed execution
     * @param logger the logger to use for error logging
     * @param <T> the return type
     * @return ResponseEntity with standardized response
     */
    public static <T> ResponseEntity<Map<String, Object>> executeWithErrorHandling(
            Supplier<T> operation,
            String successMessage,
            String errorMessage,
            Logger logger) {
        
        try {
            T result = operation.get();
            return ResponseEntity.ok(ResponseUtils.successResponse(successMessage, result));
        } catch (IllegalArgumentException e) {
            logger.warn("{}: {}", errorMessage, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ResponseUtils.errorResponse("Validation error", e.getMessage()));
        } catch (Exception e) {
            logger.error("{}", errorMessage, e);
            return ResponseEntity.internalServerError()
                .body(ResponseUtils.errorResponse(errorMessage, e.getMessage()));
        }
    }

    /**
     * Execute a service operation with custom error handling
     * @param operation the operation to execute
     * @param successMessage message for successful execution
     * @param errorMessage message for failed execution
     * @param logger the logger to use for error logging
     * @param successStatus HTTP status for success
     * @param <T> the return type
     * @return ResponseEntity with standardized response
     */
    public static <T> ResponseEntity<Map<String, Object>> executeWithErrorHandling(
            Supplier<T> operation,
            String successMessage,
            String errorMessage,
            Logger logger,
            HttpStatus successStatus) {
        
        try {
            T result = operation.get();
            return ResponseEntity.status(successStatus)
                .body(ResponseUtils.successResponse(successMessage, result));
        } catch (IllegalArgumentException e) {
            logger.warn("{}: {}", errorMessage, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ResponseUtils.errorResponse("Validation error", e.getMessage()));
        } catch (Exception e) {
            logger.error("{}", errorMessage, e);
            return ResponseEntity.internalServerError()
                .body(ResponseUtils.errorResponse(errorMessage, e.getMessage()));
        }
    }

    /**
     * Execute a void operation with standard error handling
     * @param operation the operation to execute
     * @param successMessage message for successful execution
     * @param errorMessage message for failed execution
     * @param logger the logger to use for error logging
     * @return ResponseEntity with standardized response
     */
    public static ResponseEntity<Map<String, Object>> executeVoidWithErrorHandling(
            Runnable operation,
            String successMessage,
            String errorMessage,
            Logger logger) {
        
        try {
            operation.run();
            return ResponseEntity.ok(ResponseUtils.successResponse(successMessage, null));
        } catch (IllegalArgumentException e) {
            logger.warn("{}: {}", errorMessage, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ResponseUtils.errorResponse("Validation error", e.getMessage()));
        } catch (Exception e) {
            logger.error("{}", errorMessage, e);
            return ResponseEntity.internalServerError()
                .body(ResponseUtils.errorResponse(errorMessage, e.getMessage()));
        }
    }

    /**
     * Execute an operation with health check response logic
     * @param operation the operation to execute
     * @param successMessage message for successful execution
     * @param errorMessage message for failed execution
     * @param logger the logger to use for error logging
     * @param <T> the return type
     * @return ResponseEntity with health-appropriate status codes
     */
    public static <T> ResponseEntity<Map<String, Object>> executeHealthCheck(
            Supplier<T> operation,
            String successMessage,
            String errorMessage,
            Logger logger) {
        
        try {
            T result = operation.get();
            
            // For health checks, assume healthy if no exception and result exists
            boolean isHealthy = result != null;
            
            if (isHealthy) {
                return ResponseEntity.ok(ResponseUtils.successResponse(successMessage, result));
            } else {
                return ResponseEntity.status(503)
                    .body(ResponseUtils.errorResponse(errorMessage, "Health check failed"));
            }
        } catch (Exception e) {
            logger.error("{}", errorMessage, e);
            return ResponseEntity.status(503)
                .body(ResponseUtils.errorResponse(errorMessage, e.getMessage()));
        }
    }

    /**
     * Log operation start for debugging
     * @param logger the logger to use
     * @param operation description of the operation
     */
    public static void logOperationStart(Logger logger, String operation) {
        logger.debug("Starting operation: {}", operation);
    }

    /**
     * Log operation success for debugging
     * @param logger the logger to use
     * @param operation description of the operation
     */
    public static void logOperationSuccess(Logger logger, String operation) {
        logger.debug("Successfully completed operation: {}", operation);
    }

    /**
     * Log operation failure
     * @param logger the logger to use
     * @param operation description of the operation
     * @param error the error that occurred
     */
    public static void logOperationFailure(Logger logger, String operation, Throwable error) {
        logger.error("Failed to complete operation: {}", operation, error);
    }
}
package com.example.backend.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response utility class for creating consistent API responses
 * Provides standardized response formats across all controllers
 */
@UtilityClass
public class ResponseUtils {

    /**
     * Creates a success response with data
     * @param data the response data
     * @param <T> the type of data
     * @return ResponseEntity with success status
     */
    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Creates a success response with custom status
     * @param data the response data
     * @param status the HTTP status
     * @param <T> the type of data
     * @return ResponseEntity with custom status
     */
    public static <T> ResponseEntity<T> success(T data, HttpStatus status) {
        return ResponseEntity.status(status).body(data);
    }

    /**
     * Creates a created response (201) with data
     * @param data the created resource
     * @param <T> the type of data
     * @return ResponseEntity with 201 status
     */
    public static <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    /**
     * Creates a no content response (204)
     * @return ResponseEntity with 204 status
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a not found response (404) with message
     * @param message the error message
     * @return ResponseEntity with 404 status
     */
    public static ResponseEntity<Map<String, Object>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a bad request response (400) with message
     * @param message the error message
     * @return ResponseEntity with 400 status
     */
    public static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates an internal server error response (500) with message
     * @param message the error message
     * @return ResponseEntity with 500 status
     */
    public static ResponseEntity<Map<String, Object>> internalServerError(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a custom error response
     * @param message the error message
     * @param status the HTTP status
     * @return ResponseEntity with error details
     */
    public static ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Creates a paginated response with metadata
     * @param page the page data
     * @param <T> the type of page content
     * @return ResponseEntity with pagination metadata
     */
    public static <T> ResponseEntity<Map<String, Object>> paginated(Page<T> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("pagination", createPaginationMetadata(page));
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a list response with count metadata
     * @param items the list of items
     * @param <T> the type of items
     * @return ResponseEntity with list and count
     */
    public static <T> ResponseEntity<Map<String, Object>> list(List<T> items) {
        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("count", items.size());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a simple message response
     * @param message the message
     * @return ResponseEntity with message
     */
    public static ResponseEntity<Map<String, String>> message(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a simple message response with custom status
     * @param message the message
     * @param status the HTTP status
     * @return ResponseEntity with message and custom status
     */
    public static ResponseEntity<Map<String, String>> message(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Creates pagination metadata from a Page object
     * @param page the page object
     * @param <T> the type of page content
     * @return pagination metadata map
     */
    private static <T> Map<String, Object> createPaginationMetadata(Page<T> page) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page.getNumber());
        pagination.put("size", page.getSize());
        pagination.put("totalElements", page.getTotalElements());
        pagination.put("totalPages", page.getTotalPages());
        pagination.put("first", page.isFirst());
        pagination.put("last", page.isLast());
        pagination.put("hasNext", page.hasNext());
        pagination.put("hasPrevious", page.hasPrevious());
        return pagination;
    }

    /**
     * Creates a success response with custom metadata
     * @param data the response data
     * @param metadata additional metadata
     * @param <T> the type of data
     * @return ResponseEntity with data and metadata
     */
    public static <T> ResponseEntity<Map<String, Object>> successWithMetadata(T data, Map<String, Object> metadata) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("metadata", metadata);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a success response with message and data
     * @param message success message
     * @param data the response data
     * @param <T> the type of data
     * @return ResponseEntity with success response
     */
    public static <T> Map<String, Object> successResponse(String message, T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * Creates an error response with message and details
     * @param message error message
     * @param details error details
     * @return Map with error response
     */
    public static Map<String, Object> errorResponse(String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("details", details);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * Creates a paginated success response
     * @param message success message
     * @param data the list data
     * @param page current page
     * @param size page size
     * @param total total elements
     * @param <T> the type of data
     * @return Map with paginated response
     */
    public static <T> Map<String, Object> successWithPagination(String message, List<T> data, int page, int size, long total) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("totalElements", total);
        pagination.put("totalPages", (total + size - 1) / size);
        pagination.put("hasNext", (page + 1) * size < total);
        pagination.put("hasPrevious", page > 0);
        
        response.put("pagination", pagination);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
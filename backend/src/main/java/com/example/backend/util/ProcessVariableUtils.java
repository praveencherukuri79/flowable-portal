package com.example.backend.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for managing process variables.
 * Provides common operations for variable enrichment and validation.
 */
public class ProcessVariableUtils {

    /**
     * Enrich variables with initiator information
     */
    public static Map<String, Object> enrichWithInitiator(Map<String, Object> variables, String initiator) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.putIfAbsent("initiator", initiator);
        variables.putIfAbsent("startedBy", initiator);
        return variables;
    }

    /**
     * Generate and add sheetId if not present
     */
    public static Map<String, Object> ensureSheetId(Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        if (!variables.containsKey("sheetId")) {
            String sheetId = generateSheetId();
            variables.put("sheetId", sheetId);
        }
        return variables;
    }

    /**
     * Extract or generate business key from variables
     */
    public static String extractBusinessKey(Map<String, Object> variables) {
        if (variables == null) {
            return null;
        }
        
        // First try explicit businessKey
        if (variables.containsKey("businessKey")) {
            return variables.get("businessKey").toString();
        }
        
        // Fall back to sheetId
        if (variables.containsKey("sheetId")) {
            return variables.get("sheetId").toString();
        }
        
        return null;
    }

    /**
     * Generate a unique sheetId
     */
    public static String generateSheetId() {
        return "SHEET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Merge two variable maps, with newVariables taking precedence
     */
    public static Map<String, Object> mergeVariables(Map<String, Object> existingVariables, Map<String, Object> newVariables) {
        Map<String, Object> merged = new HashMap<>();
        if (existingVariables != null) {
            merged.putAll(existingVariables);
        }
        if (newVariables != null) {
            merged.putAll(newVariables);
        }
        return merged;
    }

    /**
     * Validate required variables exist
     */
    public static void validateRequiredVariables(Map<String, Object> variables, String... requiredKeys) {
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        for (String key : requiredKeys) {
            if (!variables.containsKey(key) || variables.get(key) == null) {
                throw new IllegalArgumentException("Required variable '" + key + "' is missing");
            }
        }
    }
}


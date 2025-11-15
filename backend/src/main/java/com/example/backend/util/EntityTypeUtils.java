package com.example.backend.util;

/**
 * Utility class for entity type operations
 */
public class EntityTypeUtils {
    
    private EntityTypeUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Validate entity type
     */
    public static void validateEntityType(String entityType) {
        if (!isValidEntityType(entityType)) {
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    /**
     * Check if entity type is valid
     */
    public static boolean isValidEntityType(String entityType) {
        if (entityType == null) return false;
        String normalized = entityType.toLowerCase();
        return normalized.equals("item") || 
               normalized.equals("plan") || 
               normalized.equals("product");
    }
    
    /**
     * Get plural form of entity type for response keys
     */
    public static String getPluralForm(String entityType) {
        switch (entityType.toLowerCase()) {
            case "item":
                return "items";
            case "plan":
                return "plans";
            case "product":
                return "products";
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    /**
     * Generate maker formKey from entity type
     */
    public static String getMakerFormKey(String entityType) {
        validateEntityType(entityType);
        return "/maker/" + entityType.toLowerCase() + "-edit";
    }
}


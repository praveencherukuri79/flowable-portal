package com.example.backend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for safe comparisons
 */
public class ComparisonUtils {
    
    private ComparisonUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Null-safe equals comparison for any objects (including LocalDate)
     */
    public static boolean safeEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    /**
     * Null-safe LocalDate comparison
     */
    public static boolean datesEqual(LocalDate date1, LocalDate date2) {
        return safeEquals(date1, date2);
    }
    
    /**
     * Null-safe date comparison - handles String dates properly
     * Compares only the date part, ignoring time
     */
    public static boolean datesEqual(String date1, String date2) {
        if (date1 == null && date2 == null) return true;
        if (date1 == null || date2 == null) return false;
        
        try {
            LocalDate d1 = parseDate(date1);
            LocalDate d2 = parseDate(date2);
            return d1.equals(d2);
        } catch (DateTimeParseException e) {
            // Fallback to string comparison if parsing fails
            return date1.equals(date2);
        }
    }
    
    /**
     * Parse date string to LocalDate
     * Handles multiple common formats
     */
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new DateTimeParseException("Empty date string", dateStr, 0);
        }
        
        // Try common formats
        String[] formats = {
            "yyyy-MM-dd",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss"
        };
        
        for (String format : formats) {
            try {
                if (format.contains("HH")) {
                    // Has time component - parse as LocalDateTime and get date
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format)).toLocalDate();
                } else {
                    // Date only
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
                }
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        // Last resort: try ISO format
        return LocalDate.parse(dateStr);
    }
}


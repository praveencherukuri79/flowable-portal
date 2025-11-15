package com.example.backend.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Validation utility class for common validation operations
 * Provides reusable validation methods to avoid code duplication
 */
@UtilityClass
public class ValidationUtils {

    /**
     * Validates that a string is not null or empty
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Validates that an object is not null
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a collection is not null or empty
     * @param collection the collection to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNotEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Alias for validateNotNull for consistency
     * @param value the object to validate
     * @param message the error message
     * @throws IllegalArgumentException if validation fails
     */
    public static void requireNonNull(Object value, String message) {
        validateNotNull(value, message);
    }

    /**
     * Alias for validateNotEmpty for consistency
     * @param value the string to validate
     * @param message the error message
     * @throws IllegalArgumentException if validation fails
     */
    public static void requireNonEmpty(String value, String message) {
        validateNotEmpty(value, message);
    }

    /**
     * Alias for validateNotEmpty for collections for consistency
     * @param collection the collection to validate
     * @param message the error message
     * @throws IllegalArgumentException if validation fails
     */
    public static void requireNonEmpty(Collection<?> collection, String message) {
        validateNotEmpty(collection, message);
    }

    /**
     * Validates that a number is positive
     * @param value the number to validate
     * @param message the error message
     * @throws IllegalArgumentException if validation fails
     */
    public static void requirePositive(Number value, String message) {
        if (value == null || value.doubleValue() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a number is non-negative
     * @param value the number to validate
     * @param message the error message
     * @throws IllegalArgumentException if validation fails
     */
    public static void requireNonNegative(Number value, String message) {
        if (value == null || value.doubleValue() < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a number is positive
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePositive(Number value, String fieldName) {
        validateNotNull(value, fieldName);
        if (value.doubleValue() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validates that a number is not negative
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateNotNegative(Number value, String fieldName) {
        validateNotNull(value, fieldName);
        if (value.doubleValue() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    /**
     * Validates a condition and throws an exception with a custom message if false
     * @param condition the condition to validate
     * @param messageSupplier supplier for the error message
     * @throws IllegalArgumentException if condition is false
     */
    public static void validateCondition(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    /**
     * Validates a condition and throws an exception with a standard message if false
     * @param condition the condition to validate
     * @param message the error message
     * @throws IllegalArgumentException if condition is false
     */
    public static void validateCondition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string matches a pattern
     * @param value the string to validate
     * @param pattern the regex pattern
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePattern(String value, String pattern, String fieldName) {
        validateNotEmpty(value, fieldName);
        if (!value.matches(pattern)) {
            throw new IllegalArgumentException(fieldName + " does not match the required pattern");
        }
    }

    /**
     * Validates that a string has a minimum and maximum length
     * @param value the string to validate
     * @param minLength minimum length
     * @param maxLength maximum length
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateLength(String value, int minLength, int maxLength, String fieldName) {
        validateNotNull(value, fieldName);
        if (value.length() < minLength || value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be between " + minLength + " and " + maxLength + " characters");
        }
    }

    /**
     * Validates email format
     * @param email the email to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateEmail(String email, String fieldName) {
        validateNotEmpty(email, fieldName);
        String emailPattern = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!email.matches(emailPattern)) {
            throw new IllegalArgumentException(fieldName + " must be a valid email address");
        }
    }
}
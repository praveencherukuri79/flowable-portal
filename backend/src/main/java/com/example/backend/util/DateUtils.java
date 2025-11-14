package com.example.backend.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Date utility class for common date/time operations
 * Provides reusable date handling methods across the application
 */
@UtilityClass
public class DateUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "MMM dd, yyyy HH:mm:ss";

    /**
     * Converts Date to LocalDateTime
     * @param date the Date object
     * @return LocalDateTime or null if date is null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converts LocalDateTime to Date
     * @param localDateTime the LocalDateTime object
     * @return Date or null if localDateTime is null
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts LocalDate to Date
     * @param localDate the LocalDate object
     * @return Date or null if localDate is null
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Gets current date as formatted string
     * @param format the date format pattern
     * @return formatted current date string
     */
    public static String getCurrentDateString(String format) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Gets current date as default formatted string
     * @return formatted current date string (yyyy-MM-dd)
     */
    public static String getCurrentDateString() {
        return getCurrentDateString(DEFAULT_DATE_FORMAT);
    }

    /**
     * Gets current date and time as formatted string
     * @param format the datetime format pattern
     * @return formatted current datetime string
     */
    public static String getCurrentDateTimeString(String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Gets current date and time as default formatted string
     * @return formatted current datetime string (yyyy-MM-dd HH:mm:ss)
     */
    public static String getCurrentDateTimeString() {
        return getCurrentDateTimeString(DEFAULT_DATETIME_FORMAT);
    }

    /**
     * Formats a Date object to string
     * @param date the Date object
     * @param format the format pattern
     * @return formatted date string or null if date is null
     */
    public static String formatDate(Date date, String format) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Formats a Date object to default string format
     * @param date the Date object
     * @return formatted date string (yyyy-MM-dd HH:mm:ss) or null if date is null
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * Formats a LocalDateTime to string
     * @param dateTime the LocalDateTime object
     * @param format the format pattern
     * @return formatted datetime string or null if dateTime is null
     */
    public static String formatDateTime(LocalDateTime dateTime, String format) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Formats a LocalDate to string
     * @param date the LocalDate object
     * @param format the format pattern
     * @return formatted date string or null if date is null
     */
    public static String formatDate(LocalDate date, String format) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Parses a date string to Date object
     * @param dateString the date string
     * @param format the format pattern
     * @return Date object or null if parsing fails
     */
    public static Date parseDate(String dateString, String format) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format));
            return toDate(localDateTime);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a date string to LocalDateTime
     * @param dateString the date string
     * @param format the format pattern
     * @return LocalDateTime object or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateString, String format) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Checks if a date is in the past
     * @param date the Date object
     * @return true if date is in the past, false otherwise
     */
    public static boolean isInPast(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }

    /**
     * Checks if a date is in the future
     * @param date the Date object
     * @return true if date is in the future, false otherwise
     */
    public static boolean isInFuture(Date date) {
        if (date == null) {
            return false;
        }
        return date.after(new Date());
    }

    /**
     * Checks if a LocalDateTime is in the past
     * @param dateTime the LocalDateTime object
     * @return true if datetime is in the past, false otherwise
     */
    public static boolean isInPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Checks if a LocalDateTime is in the future
     * @param dateTime the LocalDateTime object
     * @return true if datetime is in the future, false otherwise
     */
    public static boolean isInFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Adds days to a Date
     * @param date the original date
     * @param days number of days to add
     * @return new Date with added days or null if date is null
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return toDate(localDateTime.plusDays(days));
    }

    /**
     * Adds hours to a Date
     * @param date the original date
     * @param hours number of hours to add
     * @return new Date with added hours or null if date is null
     */
    public static Date addHours(Date date, int hours) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return toDate(localDateTime.plusHours(hours));
    }

    /**
     * Gets the start of day for a given date
     * @param date the date
     * @return Date representing start of day (00:00:00) or null if date is null
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return toDate(localDateTime.toLocalDate().atStartOfDay());
    }

    /**
     * Gets the end of day for a given date
     * @param date the date
     * @return Date representing end of day (23:59:59) or null if date is null
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return toDate(localDateTime.toLocalDate().atTime(23, 59, 59, 999999999));
    }
}
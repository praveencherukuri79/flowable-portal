# Troubleshooting Login 500 Error

## Issue
Getting 500 Internal Server Error when trying to login.

## Possible Causes

### 1. Spring Security Configuration Issue
The `AuthenticationManager` might not be properly configured or there's a circular dependency.

### 2. Database Not Initialized
The H2 database might not have the users table created yet.

### 3. DataInitializer Not Running
The default users might not be created.

## Quick Fixes

### Fix 1: Check Backend Console
Look for these errors in the backend console:
- `NullPointerException`
- `BadCredentialsException`
- `UsernameNotFoundException`
- SQL errors

### Fix 2: Verify Database
Check H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

Query: `SELECT * FROM USERS;`

### Fix 3: Test with Swagger
Go to: http://localhost:8080/swagger-ui.html
Try the login endpoint there to see the actual error message.

## Most Likely Issue

Based on the error structure, it's hitting the generic exception handler in `GlobalExceptionHandler.java`.

**Common cause:** The `AuthenticationManager.authenticate()` is throwing an exception, likely:
- User not found in database
- Password encoding mismatch
- Spring Security not fully initialized

## Solution

Check if the `DataInitializer` ran successfully. Look for this log message:
```
Created user: admin with role: ADMIN
Created user: maker1 with role: MAKER
Created user: checker1 with role: CHECKER
```

If these logs are missing, the users weren't created!


package com.example.backend.controller;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin endpoints for managing users")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {
    
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users in the system")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve a specific user by username")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
    
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody RegisterRequest request) {
        userService.register(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("username", request.getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user
    ) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        
        return ResponseEntity.ok(response);
    }
}


package com.example.backend.service;

import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;

import java.util.List;

public interface UserService {
    AuthResponse login(AuthRequest request);
    AuthResponse register(RegisterRequest request);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void deleteUser(Long id);
    User updateUser(Long id, User user);
}


package com.example.backend.config;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Create default users if they don't exist
        createUserIfNotExists("maker1", "password123", "maker1@example.com", "Maker One", User.Role.MAKER);
        createUserIfNotExists("maker2", "password123", "maker2@example.com", "Maker Two", User.Role.MAKER);
        createUserIfNotExists("checker1", "password123", "checker1@example.com", "Checker One", User.Role.CHECKER);
        createUserIfNotExists("checker2", "password123", "checker2@example.com", "Checker Two", User.Role.CHECKER);
        createUserIfNotExists("admin", "admin123", "admin@example.com", "Administrator", User.Role.ADMIN);
        
        log.info("Default users initialized");
        log.info("Login with: maker1/password123, maker2/password123, checker1/password123, checker2/password123, admin/admin123");
    }
    
    private void createUserIfNotExists(String username, String password, String email, String fullName, User.Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .fullName(fullName)
                    .role(role)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("Created user: {} with role: {}", username, role);
        }
    }
}


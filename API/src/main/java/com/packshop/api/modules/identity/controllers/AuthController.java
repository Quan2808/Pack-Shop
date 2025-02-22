package com.packshop.api.modules.identity.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.modules.identity.dto.AuthRequest;
import com.packshop.api.modules.identity.dto.AuthResponse;
import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.identity.repositories.UserRepository;
import com.packshop.api.modules.identity.services.UserService;
import com.packshop.api.modules.identity.utilities.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            String token = jwtUtil.generateToken(authRequest.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(authRequest.getUsername());

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRefreshToken(refreshToken);
            response.setUsername(authRequest.getUsername());
            response.setMessage("Login successful");

            User user = userRepository.findByUsername(authRequest.getUsername()).get();
            response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userService.save(user);

            AuthResponse response = new AuthResponse();
            response.setUsername(savedUser.getUsername());
            response.setRoles(savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
            response.setMessage("User registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(refreshToken);
                if (jwtUtil.validateToken(refreshToken, username)) {
                    String newAccessToken = jwtUtil.generateToken(username);

                    AuthResponse response = new AuthResponse();
                    response.setToken(newAccessToken);
                    response.setRefreshToken(refreshToken); // Giữ nguyên refresh token cũ
                    response.setUsername(username);
                    response.setMessage("Token refreshed successfully");

                    User user = userRepository.findByUsername(username).get();
                    response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

                    return ResponseEntity.ok(response);
                } else {
                    AuthResponse response = new AuthResponse();
                    response.setMessage("Invalid refresh token");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } catch (Exception e) {
                AuthResponse response = new AuthResponse();
                response.setMessage("Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } else {
            AuthResponse response = new AuthResponse();
            response.setMessage("No refresh token provided");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<AuthResponse> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordRequest) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (!jwtUtil.validateToken(token, username)) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String newPassword = passwordRequest.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            AuthResponse response = new AuthResponse();
            response.setMessage("New password is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User updatedUser = userService.updatePassword(username, newPassword);
        AuthResponse response = new AuthResponse();
        response.setUsername(updatedUser.getUsername());
        response.setRoles(updatedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setMessage("Password updated successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).get();

            AuthResponse response = new AuthResponse();
            response.setUsername(username);
            response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
            response.setMessage("User info retrieved successfully");

            return ResponseEntity.ok(response);
        } else {
            AuthResponse response = new AuthResponse();
            response.setMessage("Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
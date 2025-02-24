package com.packshop.api.modules.identity.controllers;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.dto.AuthRegisterRequest;
import com.packshop.api.modules.identity.dto.AuthRequest;
import com.packshop.api.modules.identity.dto.AuthResponse;
import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.identity.repositories.UserRepository;
import com.packshop.api.modules.identity.services.UserService;
import com.packshop.api.modules.identity.utilities.JwtUtil;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_INVALID_MESSAGE = "Invalid or missing token";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        User user = getUserByUsername(authRequest.getUsername());
        String token = jwtUtil.generateToken(authRequest.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(authRequest.getUsername());

        return ResponseEntity.ok(buildAuthResponse(user, token, refreshToken, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest authRequest) {
        User user = User.builder()
                .username(authRequest.getUsername())
                .password(authRequest.getPassword())
                .email(authRequest.getEmail())
                .fullName(authRequest.getFullName())
                .phoneNumber(authRequest.getPhoneNumber())
                .avatarUrl(authRequest.getAvatarUrl())
                .build();

        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(savedUser, null, null, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = extractToken(authHeader);
        if (refreshToken == null) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "No refresh token provided");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        if (!jwtUtil.validateToken(refreshToken, username)) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        User user = getUserByUsername(username);
        String newAccessToken = jwtUtil.generateToken(username);
        return ResponseEntity.ok(buildAuthResponse(user, newAccessToken, refreshToken, "Token refreshed successfully"));
    }

    @PutMapping("/update-password")
    public ResponseEntity<AuthResponse> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordRequest) {
        String token = extractToken(authHeader);
        if (token == null) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, TOKEN_INVALID_MESSAGE);
        }

        String username = jwtUtil.extractUsername(token);
        if (!jwtUtil.validateToken(token, username)) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String newPassword = passwordRequest.getOrDefault("newPassword", "").trim();
        if (newPassword.isEmpty()) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "New password is required");
        }

        User updatedUser = userService.updatePassword(username, newPassword);
        return ResponseEntity.ok(buildAuthResponse(updatedUser, null, null, "Password updated successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        User user = getUserByUsername(authentication.getName());
        return ResponseEntity.ok(buildAuthResponse(user, null, null, "User info retrieved successfully"));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String extractToken(String authHeader) {
        return (authHeader != null && authHeader.startsWith(BEARER_PREFIX))
                ? authHeader.substring(BEARER_PREFIX.length())
                : null;
    }

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken, String message) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .token(token)
                .refreshToken(refreshToken)
                .message(message)
                .build();
    }

    private ResponseEntity<AuthResponse> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(AuthResponse.builder()
                        .message(message)
                        .build());
    }
}
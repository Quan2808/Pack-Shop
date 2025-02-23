package com.packshop.api.modules.identity.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        String token = jwtUtil.generateToken(authRequest.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(authRequest.getUsername());

        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setMessage("Login successful");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest authRequest) {
        User user = new User();
        user.setUsername(authRequest.getUsername());
        user.setPassword(authRequest.getPassword());
        user.setEmail(authRequest.getEmail());
        user.setFullName(authRequest.getFullName());

        User savedUser = userService.save(user);

        AuthResponse response = new AuthResponse();
        response.setUsername(savedUser.getUsername());
        response.setFullName(savedUser.getFullName());
        response.setRoles(savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setMessage("User registered successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            AuthResponse response = new AuthResponse();
            response.setMessage("No refresh token provided");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String refreshToken = authHeader.substring(7);
        String username = jwtUtil.extractUsername(refreshToken);
        if (!jwtUtil.validateToken(refreshToken, username)) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String newAccessToken = jwtUtil.generateToken(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(username);
        response.setFullName(user.getFullName());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setMessage("Token refreshed successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-password")
    public ResponseEntity<AuthResponse> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Invalid or missing token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (!jwtUtil.validateToken(token, username)) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String newPassword = passwordRequest.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            AuthResponse response = new AuthResponse();
            response.setMessage("New password is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User updatedUser = userService.updatePassword(username, newPassword);
        AuthResponse response = new AuthResponse();
        response.setUsername(updatedUser.getUsername());
        response.setFullName(updatedUser.getFullName());
        response.setRoles(updatedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setMessage("Password updated successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setUsername(username);
        response.setFullName(user.getFullName());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        response.setMessage("User info retrieved successfully");

        return ResponseEntity.ok(response);
    }
}
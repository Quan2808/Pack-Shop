package com.packshop.api.modules.identity.controllers;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.packshop.api.modules.identity.dto.AuthRequest;
import com.packshop.api.modules.identity.dto.AuthResponse;
import com.packshop.api.modules.identity.dto.SignupRequest;
import com.packshop.api.modules.identity.dto.UpdateAccountRequest;
import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.identity.repositories.UserRepository;
import com.packshop.api.modules.identity.services.UserService;
import com.packshop.api.modules.identity.utilities.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_INVALID_MESSAGE = "Invalid or missing token";
    private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred";

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        authenticate(authRequest.getUsername(), authRequest.getPassword());
        User user = findUserByUsername(authRequest.getUsername());
        return okResponse(user, jwtUtil.generateToken(user.getUsername()),
                jwtUtil.generateRefreshToken(user.getUsername()), "Login successful");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest authRequest) {
        User user = userService.save(buildUserFromSignup(authRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(user, null, null, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        String refreshToken = extractToken(authHeader);
        if (refreshToken == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "No refresh token provided");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        if (!jwtUtil.validateToken(refreshToken, username)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        User user = findUserByUsername(username);
        return okResponse(user, jwtUtil.generateToken(username), refreshToken,
                "Token refreshed successfully");
    }

    @PutMapping("/update-password")
    public ResponseEntity<AuthResponse> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordRequest) {
        return handleAuthenticatedRequest(authHeader, username -> {
            String oldPassword = passwordRequest.getOrDefault("oldPassword", "").trim();
            String newPassword = passwordRequest.getOrDefault("newPassword", "").trim();
            User updatedUser = userService.updatePassword(username, oldPassword, newPassword);
            return buildAuthResponse(updatedUser, null, null, "Password updated successfully");
        });
    }

    @PutMapping("/update-profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateAccountRequest profileRequest) {
        return handleAuthenticatedRequest(authHeader, username -> {
            User currentUser = findUserByUsername(username);
            String token = extractToken(authHeader);

            if (isProfileUnchanged(currentUser, profileRequest)) {
                log.info("No changes detected for user: {}", username);
                return buildAuthResponse(currentUser, token, null, "Nothing to change");
            }

            log.info("Updating profile for user: {}", username);
            User updatedUser = userService.updateProfile(username, profileRequest);
            return buildAuthResponse(updatedUser, token, null, "Profile updated successfully");
        });
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = findUserByUsername(authentication.getName());
        return okResponse(user, null, null, "User info retrieved successfully");
    }

    private void authenticate(String username, String password) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private User buildUserFromSignup(SignupRequest request) {
        return User.builder().username(request.getUsername()).password(request.getPassword())
                .email(request.getEmail()).fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber()).avatarUrl(request.getAvatarUrl()).build();
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String extractToken(String authHeader) {
        return (authHeader != null && authHeader.startsWith(BEARER_PREFIX))
                ? authHeader.substring(BEARER_PREFIX.length())
                : null;
    }

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken,
            String message) {
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return AuthResponse.builder().userId(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber()).avatarUrl(user.getAvatarUrl()).roles(roles)
                .token(token).refreshToken(refreshToken).message(message).build();
    }

    private ResponseEntity<AuthResponse> okResponse(User user, String token, String refreshToken,
            String message) {
        return ResponseEntity.ok(buildAuthResponse(user, token, refreshToken, message));
    }

    private ResponseEntity<AuthResponse> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(AuthResponse.builder().message(message).build());
    }

    private ResponseEntity<AuthResponse> handleAuthenticatedRequest(String authHeader,
            java.util.function.Function<String, AuthResponse> action) {
        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractUsername(token))) {
                throw new SecurityException(TOKEN_INVALID_MESSAGE);
            }
            return ResponseEntity.ok(action.apply(jwtUtil.extractUsername(token)));
        } catch (SecurityException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage());
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE);
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private boolean isProfileUnchanged(User user, UpdateAccountRequest request) {
        boolean unchanged = Objects.equals(user.getEmail(), request.getEmail())
                && Objects.equals(user.getFullName(), request.getFullName())
                && Objects.equals(user.getPhoneNumber(), request.getPhoneNumber())
                && Objects.equals(user.getAvatarUrl(), request.getAvatarUrl());
        log.debug("Profile unchanged check result: {}", unchanged);
        return unchanged;
    }
}

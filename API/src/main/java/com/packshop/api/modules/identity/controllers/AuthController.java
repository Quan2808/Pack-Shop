package com.packshop.api.modules.identity.controllers;

import java.util.Map;
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

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        authenticate(authRequest.getUsername(), authRequest.getPassword());
        User user = getUserByUsername(authRequest.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        return ResponseEntity.ok(buildAuthResponse(user, token, refreshToken, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest authRequest) {
        User user = createUserFromRequest(authRequest);
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(savedUser, null, null, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
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
        return ResponseEntity.ok(buildAuthResponse(user, newAccessToken, refreshToken,
                "Token refreshed successfully"));
    }

    @PutMapping("/update-password")
    public ResponseEntity<AuthResponse> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordRequest) {
        try {
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            String oldPassword = passwordRequest.getOrDefault("oldPassword", "").trim();
            String newPassword = passwordRequest.getOrDefault("newPassword", "").trim();
            User updatedUser = userService.updatePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok(
                    buildAuthResponse(updatedUser, null, null, "Password updated successfully"));
        } catch (SecurityException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating password", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating password");
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateAccountRequest profileRequest) {
        try {
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            User updatedUser = userService.updateProfile(username, profileRequest);
            return ResponseEntity.ok(
                    buildAuthResponse(updatedUser, token, null, "Profile updated successfully"));
        } catch (SecurityException e) {
            return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating profile: " + e.getMessage());
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating profile: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = getUserByUsername(authentication.getName());
        return ResponseEntity
                .ok(buildAuthResponse(user, null, null, "User info retrieved successfully"));
    }

    private void authenticate(String username, String password) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private User createUserFromRequest(SignupRequest authRequest) {
        return User.builder().username(authRequest.getUsername())
                .password(authRequest.getPassword()).email(authRequest.getEmail())
                .fullName(authRequest.getFullName()).phoneNumber(authRequest.getPhoneNumber())
                .avatarUrl(authRequest.getAvatarUrl()).build();
    }

    private String validateAndExtractToken(String authHeader) {
        String token = extractToken(authHeader);
        if (token == null || !isTokenValid(token)) {
            throw new SecurityException(TOKEN_INVALID_MESSAGE);
        }
        return token;
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

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken,
            String message) {
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return AuthResponse.builder().userId(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber()).avatarUrl(user.getAvatarUrl()).roles(roles)
                .token(token).refreshToken(refreshToken).message(message).build();
    }

    private ResponseEntity<AuthResponse> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(AuthResponse.builder().message(message).build());
    }

    private boolean isTokenValid(String token) {
        String username = jwtUtil.extractUsername(token);
        return jwtUtil.validateToken(token, username);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}

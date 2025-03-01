package com.packshop.api.modules.identity.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.modules.identity.dto.AuthResponse;
import com.packshop.api.modules.identity.dto.UserResponse;
import com.packshop.api.modules.identity.services.UserManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService service;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = service.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<AuthResponse> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody Set<String> roleNames) {
        AuthResponse updatedUser = service.updateUserRoles(userId, roleNames);
        return ResponseEntity.ok(updatedUser);
    }
}

package com.packshop.api.modules.identity.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.dto.AuthResponse;
import com.packshop.api.modules.identity.dto.UserResponse;
import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.identity.repositories.RoleRepository;
import com.packshop.api.modules.identity.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> result = users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
        // log.info("Successfully fetched {} users", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = findUserByUserId(userId);
        UserResponse result = modelMapper.map(user, UserResponse.class);
        return result;
    }

    @Transactional
    public AuthResponse updateUserRoles(Long userId, Set<String> roleNames) {
        User user = findUserByUserId(userId);

        Set<String> userRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        if (userRoles.equals(roleNames)) {
            AuthResponse result = modelMapper.map(user, AuthResponse.class);
            result.setMessage("User already has the requested roles. Nothing to change");
            return result;
        }

        Set<Role> roles = getRoles(roleNames);
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);

        AuthResponse result = modelMapper.map(updatedUser, AuthResponse.class);
        result.setMessage("Successfully updated roles for user " + user.getUsername());
        return result;
    }

    private User findUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found: {}", roleName);
                    return new ResourceNotFoundException("Role not found: " + roleName);
                });
    }

    private Set<Role> getRoles(Set<String> roleNames) {
        return roleNames.stream()
                .map(this::findRoleByName)
                .collect(Collectors.toSet());
    }
}

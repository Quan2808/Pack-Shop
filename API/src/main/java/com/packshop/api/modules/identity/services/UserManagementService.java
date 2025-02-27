package com.packshop.api.modules.identity.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
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
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        List<UserResponse> result = users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
        log.info("Successfully fetched {} users", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        UserResponse result = modelMapper.map(user, UserResponse.class);

        return result;
    }

    @Transactional
    public UserResponse updateUserRoles(Long userId, Set<String> roleNames) {
        log.info("Updating roles for user id: {} with roles: {}", userId, roleNames);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> {
                            log.error("Role not found: {}", name);
                            return new ResourceNotFoundException("Role not found: " + name);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        UserResponse result = modelMapper.map(updatedUser, UserResponse.class);
        log.info("Successfully updated roles for user {}: {}", user.getUsername(), result.getRoles());
        return result;
    }
}

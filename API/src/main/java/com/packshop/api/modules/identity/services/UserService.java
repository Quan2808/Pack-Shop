package com.packshop.api.modules.identity.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.packshop.api.common.exceptions.DuplicateResourceException;
import com.packshop.api.modules.identity.dto.UpdateAccountRequest;
import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.identity.repositories.RoleRepository;
import com.packshop.api.modules.identity.repositories.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ROLE = "USER";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = checkUserExist(username);
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), mapRolesToAuthorities(user.getRoles()));
    }

    @Transactional
    public User save(User user) {
        checkDuplicateField(user.getUsername(), "Username", userRepository::findByUsername);
        checkDuplicateField(user.getEmail(), "Email", userRepository::findByEmail);
        checkDuplicateField(user.getPhoneNumber(), "Phone number",
                userRepository::findByPhoneNumber);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        assignDefaultRoleIfAbsent(user);
        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(String username, String oldPassword, String newPassword) {
        validatePasswordInputs(oldPassword, newPassword);
        User user = checkUserExist(username);
        verifyOldPassword(user, oldPassword);
        ensureNewPasswordIsDifferent(user, newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String username, UpdateAccountRequest profileRequest) {
        User user = checkUserExist(username);

        if (profileRequest.getEmail() != null
                && !profileRequest.getEmail().equals(user.getEmail())) {
            if (isDuplicateField(profileRequest.getEmail(), "Email", userRepository::findByEmail)) {
                throw new DuplicateResourceException(
                        "Email '" + profileRequest.getEmail() + "' is already in use");
            }
            user.setEmail(profileRequest.getEmail());
        }

        if (profileRequest.getFullName() != null
                && !profileRequest.getFullName().equals(user.getFullName())) {
            user.setFullName(profileRequest.getFullName().trim());
        }

        if (profileRequest.getPhoneNumber() != null
                && !profileRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (isDuplicateField(profileRequest.getPhoneNumber(), "Phone number",
                    userRepository::findByPhoneNumber)) {
                throw new DuplicateResourceException(
                        "Phone number '" + profileRequest.getPhoneNumber() + "' is already in use");
            }
            user.setPhoneNumber(profileRequest.getPhoneNumber());
        }

        if (profileRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(profileRequest.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    private void assignDefaultRoleIfAbsent(User user) {
        Set<Role> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();
        if (roles.isEmpty()) {
            roles.add(roleRepository.findByName(DEFAULT_ROLE)
                    .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE))));
        }
        user.setRoles(roles);
    }

    private void validatePasswordInputs(String oldPassword, String newPassword) {
        if (StringUtils.isBlank(oldPassword)) {
            throw new SecurityException("Current password cannot be empty");
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new SecurityException("New password cannot be empty");
        }
        if (newPassword.length() < 8) {
            throw new SecurityException("New password must be at least 8 characters long");
        }
    }

    private void verifyOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new SecurityException("Current password is incorrect");
        }
    }

    private void ensureNewPasswordIsDifferent(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SecurityException("New password cannot be the same as the old password");
        }
    }

    private void checkDuplicateField(String value, String fieldName,
            Function<String, Optional<User>> checker) {
        if (value != null && checker.apply(value).isPresent()) {
            throw new DuplicateResourceException(fieldName + " '" + value + "' already exists");
        }
    }

    private boolean isDuplicateField(String value, String fieldName,
            Function<String, Optional<User>> checker) {
        return value != null && checker.apply(value).isPresent();
    }

    private User checkUserExist(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private List<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}

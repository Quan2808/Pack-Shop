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
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUserByUsername(username);
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), mapRolesToAuthorities(user.getRoles()));
    }

    @Transactional
    public User save(User user) {
        validateUniqueFields(user.getUsername(), user.getEmail(), user.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        assignDefaultRole(user);
        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(String username, String oldPassword, String newPassword) {
        validatePasswordChange(oldPassword, newPassword);
        User user = findUserByUsername(username);
        verifyOldPassword(user, oldPassword);
        ensureNewPasswordIsDifferent(user, newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String username, UpdateAccountRequest profileRequest) {
        User user = findUserByUsername(username);
        updateFieldIfChanged(user::setEmail, user.getEmail(), profileRequest.getEmail(), "Email",
                userRepository::findByEmail);
        updateFieldIfChanged(user::setFullName, user.getFullName(), profileRequest.getFullName(),
                null, null, String::trim);
        updateFieldIfChanged(user::setPhoneNumber, user.getPhoneNumber(),
                profileRequest.getPhoneNumber(), "Phone number", userRepository::findByPhoneNumber);
        updateFieldIfChanged(user::setAvatarUrl, user.getAvatarUrl(), profileRequest.getAvatarUrl(),
                null, null);
        return userRepository.save(user);
    }

    private void validateUniqueFields(String username, String email, String phoneNumber) {
        checkDuplicate(username, "Username", userRepository::findByUsername);
        checkDuplicate(email, "Email", userRepository::findByEmail);
        checkDuplicate(phoneNumber, "Phone number", userRepository::findByPhoneNumber);
    }

    private void assignDefaultRole(User user) {
        Set<Role> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();
        if (roles.isEmpty()) {
            roles.add(roleRepository.findByName(DEFAULT_ROLE)
                    .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE))));
        }
        user.setRoles(roles);
    }

    private void validatePasswordChange(String oldPassword, String newPassword) {
        if (StringUtils.isBlank(oldPassword))
            throw new SecurityException("Current password cannot be empty");
        if (StringUtils.isBlank(newPassword))
            throw new SecurityException("New password cannot be empty");
        if (newPassword.length() < MIN_PASSWORD_LENGTH)
            throw new SecurityException(
                    "New password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
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

    private void checkDuplicate(String value, String fieldName,
            Function<String, Optional<User>> checker) {
        if (value != null && checker.apply(value).isPresent()) {
            throw new DuplicateResourceException(fieldName + " '" + value + "' already exists");
        }
    }

    private boolean isDuplicate(String value, Function<String, Optional<User>> checker) {
        return value != null && checker.apply(value).isPresent();
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private List<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    private <T> void updateFieldIfChanged(java.util.function.Consumer<T> setter, T currentValue,
            T newValue, String fieldName, Function<String, Optional<User>> duplicateChecker,
            java.util.function.UnaryOperator<T> transformer) {
        if (newValue != null && !newValue.equals(currentValue)) {
            T transformedValue = transformer != null ? transformer.apply(newValue) : newValue;
            if (fieldName != null && duplicateChecker != null
                    && isDuplicate((String) transformedValue, duplicateChecker)) {
                throw new SecurityException(
                        fieldName + " '" + transformedValue + "' is already in use");
            }
            setter.accept(transformedValue);
        }
    }

    private <T> void updateFieldIfChanged(java.util.function.Consumer<T> setter, T currentValue,
            T newValue, String fieldName, Function<String, Optional<User>> duplicateChecker) {
        updateFieldIfChanged(setter, currentValue, newValue, fieldName, duplicateChecker, null);
    }
}

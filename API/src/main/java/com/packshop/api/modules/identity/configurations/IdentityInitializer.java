package com.packshop.api.modules.identity.configurations;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.identity.repositories.RoleRepository;
import com.packshop.api.modules.identity.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class IdentityInitializer {

    private static final Set<String> DEFAULT_ROLES = Set.of("ADMIN", "USER");

    private String ADMIN_USERNAME = "admin";
    private String ADMIN_PASSWORD = "admin";
    private String ADMIN_EMAIL = "admin@example.com";
    private String ADMIN_FULL_NAME = "Administrator";
    private String ADMIN_PHONE_NUMBER = "(+84) 123 456 789";

    @Bean
    public CommandLineRunner initializeIdentity(RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Skipping initialization Roles and admin account if already exist
            if (areRolesInitialized(roleRepository) && isAdminInitialized(userRepository)) {
                log.info("Roles and admin account already exist. Skipping initialization.");
                return;
            }

            log.info("Starting identity initialization...");

            // Step 1: Initialize roles
            initializeDefaultRoles(roleRepository);

            // Step 2: Initialize admin
            initializeDefaultAdmin(userRepository, roleRepository, passwordEncoder);

            log.info("Identity initialization completed.");
        };
    }

    private boolean areRolesInitialized(RoleRepository roleRepository) {
        return DEFAULT_ROLES.stream()
                .allMatch(roleName -> roleRepository.findByName(roleName).isPresent());
    }

    private boolean isAdminInitialized(UserRepository userRepository) {
        return userRepository.findByUsername(ADMIN_USERNAME).isPresent() ||
                userRepository.findByEmail(ADMIN_EMAIL).isPresent();
    }

    private void initializeDefaultRoles(RoleRepository roleRepository) {
        DEFAULT_ROLES.forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.debug("Created role: {}", roleName);
            } else {
                log.debug("Role {} already exists, skipping creation.", roleName);
            }
        });
    }

    private void initializeDefaultAdmin(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent() ||
                userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            log.debug("Admin account with username={} or email={} already exists, skipping creation.",
                    ADMIN_USERNAME, ADMIN_EMAIL);
            return;
        }

        Optional<Role> adminRoleOptional = roleRepository.findByName("ADMIN");
        if (adminRoleOptional.isEmpty()) {
            log.error("ADMIN role not found after initialization!");
            throw new IllegalStateException("ADMIN role is required but not found");
        }
        Role adminRole = adminRoleOptional.get();

        User admin = buildAdminUser(passwordEncoder, adminRole);
        userRepository.save(admin);
        log.info("Created default admin account: username={}, email={}", ADMIN_USERNAME, ADMIN_EMAIL);
    }

    private User buildAdminUser(PasswordEncoder passwordEncoder, Role adminRole) {
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEmail(ADMIN_EMAIL);
        admin.setFullName(ADMIN_FULL_NAME);
        admin.setPhoneNumber(ADMIN_PHONE_NUMBER);
        admin.setRoles(Collections.singleton(adminRole));
        return admin;
    }
}
package com.packshop.api.modules.identity.configurations;

import java.util.HashSet;
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
public class AdminInitializer {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_EMAIL = "admin@example.com";

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Initializing default admin account...");

            Optional<Role> adminRoleOptional = roleRepository.findByName("ADMIN");
            if (adminRoleOptional.isEmpty()) {
                log.warn("ADMIN role not found. Please ensure roles are initialized first.");
            }
            Role adminRole = adminRoleOptional.get();

            if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
                User admin = new User();
                admin.setUsername(ADMIN_USERNAME);
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                admin.setEmail(ADMIN_EMAIL);

                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                admin.setRoles(adminRoles);

                userRepository.save(admin);
                log.info("Created default admin account: username={}, email={}", ADMIN_USERNAME, ADMIN_EMAIL);
            } else {
                log.info("Admin account already exists: username={}", ADMIN_USERNAME);
            }
            log.info("Admin initialization completed.");
        };
    }
}

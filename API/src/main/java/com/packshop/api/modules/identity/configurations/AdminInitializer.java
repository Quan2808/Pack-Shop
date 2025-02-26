package com.packshop.api.modules.identity.configurations;

import java.util.Collections;
import java.util.Optional;
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
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_FULL_NAME = "Administrator";
    private static final String ADMIN_PHONE_NUMBER = "(+84) 123 456 789";
    private static final String ADMIN_AVATAR_URL = "https://example.com/avatar/admin.png";

    @Bean
    public CommandLineRunner initializeAdmin(UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Starting admin account initialization...");
            initializeDefaultAdmin(userRepository, roleRepository, passwordEncoder);
            log.info("Admin initialization completed.");
        };
    }

    private void initializeDefaultAdmin(UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        // Kiểm tra xem admin đã tồn tại
        if (!userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            log.info("Admin account already exists: username={}", ADMIN_USERNAME);
            return;
        }

        // Lấy role ADMIN
        Optional<Role> adminRoleOptional = roleRepository.findByName("ADMIN");
        if (adminRoleOptional.isEmpty()) {
            log.warn("ADMIN role not found. Please initialize roles first.");
            return;
        }
        Role adminRole = adminRoleOptional.get();

        // Tạo admin user
        User admin = buildAdminUser(passwordEncoder, adminRole);
        userRepository.save(admin);

        log.info("Created default admin account: username={}, email={}, fullName={}",
                ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_FULL_NAME);
    }

    private User buildAdminUser(PasswordEncoder passwordEncoder, Role adminRole) {
        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEmail(ADMIN_EMAIL);
        admin.setFullName(ADMIN_FULL_NAME);
        admin.setPhoneNumber(ADMIN_PHONE_NUMBER);
        admin.setAvatarUrl(ADMIN_AVATAR_URL);
        admin.setRoles(Collections.singleton(adminRole));

        return admin;
    }
}

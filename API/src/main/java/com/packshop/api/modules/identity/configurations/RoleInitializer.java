package com.packshop.api.modules.identity.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.repositories.RoleRepository;

@Configuration
public class RoleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializer.class);
    private static final String[] DEFAULT_ROLES = { "ADMIN", "USER" };

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            logger.info("Initializing default roles...");
            for (String roleName : DEFAULT_ROLES) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                    logger.info("Created role: {}", roleName);
                }
            }
            logger.info("Role initialization completed.");
        };
    }
}
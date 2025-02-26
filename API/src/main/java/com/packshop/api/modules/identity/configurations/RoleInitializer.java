package com.packshop.api.modules.identity.configurations;

import java.util.Arrays;
import java.util.List;
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
    private static final List<String> DEFAULT_ROLES = Arrays.asList("ADMIN", "USER");

    @Bean
    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {
            logger.info("Starting role initialization...");
            createDefaultRoles(roleRepository);
            logger.info("Finished initializing roles.");
        };
    }

    private void createDefaultRoles(RoleRepository roleRepository) {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role newRole = new Role();
                newRole.setName(roleName);
                roleRepository.save(newRole);
                logger.info("Successfully created role: {}", roleName);
            } else {
                logger.info("Role {} already exists, skipping...", roleName);
            }
        }
    }
}

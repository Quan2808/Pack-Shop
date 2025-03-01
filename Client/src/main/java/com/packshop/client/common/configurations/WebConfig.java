package com.packshop.client.common.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@SuppressWarnings("null") final ResourceHandlerRegistry registry) {
        String[][] resources = {
                { "/css/**", "classpath:/static/css/" },
                { "/js/**", "classpath:/static/js/" },
                { "/images/**", "classpath:/static/images/" },
                { "/plugins/**", "classpath:/static/plugins/" }
        };

        for (String[] resource : resources) {
            registry.addResourceHandler(resource[0])
                    .addResourceLocations(resource[1])
                    .setCachePeriod(3600);
            ;
        }
    }
}
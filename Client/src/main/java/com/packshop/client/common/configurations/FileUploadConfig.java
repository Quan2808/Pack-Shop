package com.packshop.client.common.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
        String uploadPath =
                "file:" + System.getProperty("user.dir") + "/src/main/resources/static/media/";

        registry.addResourceHandler("/media/**").addResourceLocations(uploadPath)
                .setCachePeriod(3600).resourceChain(true).addResolver(new PathResourceResolver());
    }
}

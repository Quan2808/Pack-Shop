package com.packshop.api.common.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.packshop.api.modules.identity.configurations.CustomAuthenticationPrincipalArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CustomAuthenticationPrincipalArgumentResolver customResolver;

    public WebConfig(CustomAuthenticationPrincipalArgumentResolver customResolver) {
        this.customResolver = customResolver;
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @SuppressWarnings("null")
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(customResolver);
    }
}

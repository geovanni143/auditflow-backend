package com.auditflow.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class CorsConfig {

    private final String allowedOriginsValue;

    public CorsConfig(@Value("${app.security.cors.allowed-origins}") String allowedOriginsValue) {
        this.allowedOriginsValue = allowedOriginsValue;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return (HttpServletRequest request) -> {
            CorsConfiguration config = new CorsConfiguration();

            List<String> allowedOrigins = Arrays.stream(allowedOriginsValue.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .toList();

            config.setAllowedOrigins(allowedOrigins);
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);

            return config;
        };
    }
}
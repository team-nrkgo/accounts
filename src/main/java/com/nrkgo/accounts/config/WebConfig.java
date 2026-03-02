package com.nrkgo.accounts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow all endpoints
                .allowedOrigins(frontendUrl) // Allow Frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow all standard methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // IMPORTANT: Allow cookies
                .maxAge(3600);
    }
}

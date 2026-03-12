package com.nrkgo.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        org.springframework.http.client.ClientHttpRequestFactory factory = new org.springframework.http.client.BufferingClientHttpRequestFactory(
                new org.springframework.http.client.SimpleClientHttpRequestFactory());

        RestTemplate restTemplate = new RestTemplate(factory);

        java.util.List<org.springframework.http.client.ClientHttpRequestInterceptor> interceptors = restTemplate
                .getInterceptors();
        if (interceptors == null) {
            interceptors = new java.util.ArrayList<>();
        }
        interceptors.add(new com.nrkgo.accounts.common.interceptor.RestTemplateLoggingInterceptor());
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

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

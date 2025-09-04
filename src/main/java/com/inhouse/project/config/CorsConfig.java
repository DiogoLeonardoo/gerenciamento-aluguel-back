package com.inhouse.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // todos os endpoints
                        .allowedOrigins(
                            "http://localhost:3000",
                            "http://127.0.0.1:3000",
                            "https://in-house-front-git-dev-diogo-leonardos-projects.vercel.app"
                        ) // origens do front (desenvolvimento e produção)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true) // permite cookies
                        .maxAge(3600); // cache por 1 hora
            }
        };
    }
}

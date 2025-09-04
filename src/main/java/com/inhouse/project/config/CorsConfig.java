package com.inhouse.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // CORS agora é gerenciado pelo SecurityConfig
    // Esta configuração pode ser removida ou mantida como backup

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // CORS desabilitado - gerenciado pelo SecurityConfig
                // registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }
}

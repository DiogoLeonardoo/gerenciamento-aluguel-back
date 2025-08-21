package com.inhouse.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {
    
    /**
     * Configuração personalizada do ObjectMapper para lidar corretamente com entidades JPA/Hibernate,
     * incluindo o tratamento adequado de LOBs, proxies Hibernate e propriedades lazy.
     */
    @Bean
    public ObjectMapper objectMapper() {
        // Módulo para tratamento de entidades Hibernate
        Hibernate5JakartaModule hibernateModule = new Hibernate5JakartaModule();
        // Não falha em propriedades lazy não inicializadas
        hibernateModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        
        // Usando Jackson2ObjectMapperBuilder para configurar
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(hibernateModule, new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        
        // Configuração adicional para evitar loops de serialização infinitos
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return objectMapper;
    }
}

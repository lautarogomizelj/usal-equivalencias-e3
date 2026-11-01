package com.usal.e3.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI e3OpenApi() {
        return new OpenAPI().info(new Info()
                .title("E3 · Equivalencias USAL")
                .description("API del Sistema de Tramitación de Equivalencias Electrónicas")
                .version("v1"));
    }
}

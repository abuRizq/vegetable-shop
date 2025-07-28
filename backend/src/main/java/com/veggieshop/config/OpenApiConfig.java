package com.veggieshop.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vegetable Shop API")
                        .version("1.0")
                        .description("Professional API documentation for Vegetable Shop backend (Spring Boot)")
                );
    }
}

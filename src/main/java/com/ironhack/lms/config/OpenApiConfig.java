package com.ironhack.lms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IronLMS API")
                        .description("A comprehensive Learning Management System API built with Spring Boot. " +
                                "This API provides endpoints for course management, user authentication, " +
                                "enrollment tracking, and assignment submissions.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ironhack Development Team")
                                .email("dev@ironhack.com")
                                .url("https://ironhack.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:3315")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.ironlms.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /auth/login endpoint")));
    }
    
    @Bean
    GroupedOpenApi ironLmsApi() {
        return GroupedOpenApi.builder()
                .group("ironlms")
                .packagesToScan("com.ironhack.lms.web")  // ONLY controllers
                .pathsToMatch("/api/**", "/auth/**")     // ONLY API routes
                .build();
    }
}

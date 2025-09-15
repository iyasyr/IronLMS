package com.ironhack.lms.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    GroupedOpenApi ironLmsApi() {
        return GroupedOpenApi.builder()
                .group("ironlms")
                .packagesToScan("com.ironhack.lms.web")  // ONLY controllers
                .pathsToMatch("/api/**", "/auth/**")     // ONLY API routes
                .build();
    }
}

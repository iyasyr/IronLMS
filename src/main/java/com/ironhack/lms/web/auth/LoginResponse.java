package com.ironhack.lms.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token")
public record LoginResponse(
        @Schema(description = "JWT token for API authentication", 
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                required = true)
        String token
) {}

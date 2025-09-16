package com.ironhack.lms.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login credentials")
public record LoginRequest(
        @Schema(description = "User's email address", example = "student@ironhack.com", required = true)
        @Email @NotBlank String email,
        
        @Schema(description = "User's password", example = "securePassword123", required = true)
        @NotBlank String password
) {}

package com.ironhack.lms.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "User Info", description = "User information and profile endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WhoAmIController {
    
    @Operation(
            summary = "Get Current User Info",
            description = "Retrieve information about the currently authenticated user including username and authorities.",
            operationId = "getCurrentUser"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user information",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "User Info",
                                    value = "{\"username\": \"student@ironhack.com\", \"authorities\": [{\"authority\": \"ROLE_STUDENT\"}]}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = "{\"error\": \"unauthorized\"}"
                            )
                    )
            )
    })
    @GetMapping("/api/me")
    public Map<String,Object> me(Authentication auth) {
        return Map.of("username", auth.getName(), "authorities", auth.getAuthorities());
    }
}

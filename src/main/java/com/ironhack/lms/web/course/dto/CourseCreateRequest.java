package com.ironhack.lms.web.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new course")
public record CourseCreateRequest(
        @Schema(description = "Course title", example = "Advanced Spring Boot Development", required = true, maxLength = 200)
        @NotBlank @Size(max = 200) String title,
        
        @Schema(description = "Detailed course description", example = "Learn advanced Spring Boot concepts including security, testing, and deployment", maxLength = 20000)
        @Size(max = 20000) String description
) {}

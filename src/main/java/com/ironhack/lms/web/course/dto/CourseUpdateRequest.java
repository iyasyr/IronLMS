package com.ironhack.lms.web.course.dto;

import com.ironhack.lms.domain.course.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing course")
public record CourseUpdateRequest(
        @Schema(description = "Updated course title", example = "Advanced Spring Boot Development - Updated", required = true, maxLength = 200)
        @NotBlank @Size(max = 200) String title,
        
        @Schema(description = "Updated course description", example = "Comprehensive course covering advanced Spring Boot concepts...", maxLength = 20000)
        @Size(max = 20000) String description,
        
        @Schema(description = "New course status", example = "PUBLISHED", required = true)
        @NotNull CourseStatus status
) {}

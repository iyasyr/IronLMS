package com.ironhack.lms.web.enrollment.dto;

import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Enrollment information response")
public record EnrollmentResponse(
        @Schema(description = "Unique enrollment identifier", example = "1")
        Long id,
        
        @Schema(description = "ID of the enrolled course", example = "2")
        Long courseId,
        
        @Schema(description = "Title of the enrolled course", example = "Introduction to Java Programming")
        String courseTitle,
        
        @Schema(description = "Current enrollment status", example = "ACTIVE")
        EnrollmentStatus status,
        
        @Schema(description = "When the enrollment was created", example = "2024-01-15T10:30:00Z")
        Instant enrolledAt
) {}

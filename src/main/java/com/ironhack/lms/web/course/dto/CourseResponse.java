package com.ironhack.lms.web.course.dto;

import com.ironhack.lms.domain.course.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Course information response")
public record CourseResponse(
        @Schema(description = "Unique course identifier", example = "1")
        Long id,
        
        @Schema(description = "ID of the instructor who created this course", example = "2")
        Long instructorId,
        
        @Schema(description = "Course title", example = "Introduction to Java Programming")
        String title,
        
        @Schema(description = "Detailed course description", example = "Learn the fundamentals of Java programming...")
        String description,
        
        @Schema(description = "Current status of the course", example = "PUBLISHED")
        CourseStatus status,
        
        @Schema(description = "When the course was created", example = "2024-01-15T10:30:00Z")
        Instant createdAt,
        
        @Schema(description = "When the course was published (null if not published)", example = "2024-01-20T14:00:00Z")
        Instant publishedAt
) {}

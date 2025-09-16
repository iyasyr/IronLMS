package com.ironhack.lms.web.course.dto;

import com.ironhack.lms.domain.course.CourseStatus;
import java.time.Instant;

public record CourseResponse(
        Long id,
        Long instructorId,
        String title,
        String description,
        CourseStatus status,
        Instant createdAt,
        Instant publishedAt
) {}

package com.ironhack.lms.web.enrollment.dto;

import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import java.time.Instant;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        Instant enrolledAt
) {}

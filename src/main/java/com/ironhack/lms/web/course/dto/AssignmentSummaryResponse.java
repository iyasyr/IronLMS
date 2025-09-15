package com.ironhack.lms.web.course.dto;

import java.time.Instant;

public record AssignmentSummaryResponse(
        Long id,
        String title,
        String instructions,
        Integer maxPoints,
        boolean allowLate,
        Instant dueAt
) {}

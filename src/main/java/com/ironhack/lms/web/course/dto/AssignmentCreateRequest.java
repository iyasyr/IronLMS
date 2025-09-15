package com.ironhack.lms.web.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record AssignmentCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 50000) String instructions,
        Instant dueAt,
        @Min(1) int maxPoints,
        boolean allowLate
) {}

package com.ironhack.lms.web.course.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record AssignmentCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 50000) String instructions,
        Instant dueAt,
        @Min(1) @Max(100000) int maxPoints,
        boolean allowLate
) {}

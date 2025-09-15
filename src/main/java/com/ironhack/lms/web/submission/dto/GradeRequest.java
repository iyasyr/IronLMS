package com.ironhack.lms.web.submission.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeRequest(
        @NotNull @Min(0) @Max(10000) Integer score,  // validated against maxPoints in service
        @Size(max = 50000) String feedback
) {}

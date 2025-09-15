package com.ironhack.lms.web.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2048) String contentUrl,
        @Min(1) int orderIndex
) {}

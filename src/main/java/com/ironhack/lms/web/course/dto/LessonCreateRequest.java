package com.ironhack.lms.web.course.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

public record LessonCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2048) @URL(message = "contentUrl must be a valid URL") String contentUrl,
        @Min(1) @Max(10000) int orderIndex
) {}

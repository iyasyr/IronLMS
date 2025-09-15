package com.ironhack.lms.web.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 20000) String description
) {}

package com.ironhack.lms.web.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionCreateRequest(
        @NotBlank @Size(max = 2048) String artifactUrl
) {}

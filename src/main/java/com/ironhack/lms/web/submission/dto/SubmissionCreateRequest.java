package com.ironhack.lms.web.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record SubmissionCreateRequest(
        @NotBlank @Size(max = 2048) @URL String artifactUrl
) {}

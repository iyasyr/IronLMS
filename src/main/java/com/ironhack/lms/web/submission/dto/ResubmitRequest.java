package com.ironhack.lms.web.submission.dto;

import jakarta.validation.constraints.Size;

public record ResubmitRequest(
        @Size(max = 50000) String feedback
) {}

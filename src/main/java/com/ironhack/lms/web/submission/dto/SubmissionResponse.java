package com.ironhack.lms.web.submission.dto;

import com.ironhack.lms.domain.submission.SubmissionStatus;
import java.time.Instant;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        Long courseId,
        Long studentId,
        Instant submittedAt,
        String artifactUrl,
        SubmissionStatus status,
        Integer score,
        String feedback,
        int version
) {}

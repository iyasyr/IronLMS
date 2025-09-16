package com.ironhack.lms.web.submission.dto;

import com.ironhack.lms.domain.submission.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Assignment submission information response")
public record SubmissionResponse(
        @Schema(description = "Unique submission identifier", example = "1")
        Long id,
        
        @Schema(description = "ID of the assignment this submission is for", example = "2")
        Long assignmentId,
        
        @Schema(description = "ID of the course containing the assignment", example = "3")
        Long courseId,
        
        @Schema(description = "ID of the student who submitted", example = "4")
        Long studentId,
        
        @Schema(description = "When the submission was made", example = "2024-01-15T10:30:00Z")
        Instant submittedAt,
        
        @Schema(description = "URL to the submitted artifact (file, repository, etc.)", example = "https://github.com/student/project")
        String artifactUrl,
        
        @Schema(description = "Current submission status", example = "SUBMITTED")
        SubmissionStatus status,
        
        @Schema(description = "Grade/score received (null if not graded yet)", example = "85")
        Integer score,
        
        @Schema(description = "Instructor feedback on the submission", example = "Good work! Consider improving error handling.")
        String feedback,
        
        @Schema(description = "Submission version number", example = "1")
        int version
) {}

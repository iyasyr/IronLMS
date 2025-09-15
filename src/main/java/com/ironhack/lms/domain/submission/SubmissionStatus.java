package com.ironhack.lms.domain.submission;

public enum SubmissionStatus {
    SUBMITTED,      // student submitted; awaiting grading
    GRADED,         // graded by instructor/admin
    RESUBMIT_REQUESTED  // instructor/admin requests a new version
}

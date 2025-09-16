package com.ironhack.lms.domain.submission;

import com.ironhack.lms.domain.course.Assignment;
import com.ironhack.lms.domain.user.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionTest {

    private Submission submission;
    private Assignment assignment;
    private Student student;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        assignment = new Assignment();
        student = new Student();
    }

    @Test
    void prePersist_shouldSetDefaultValues() {
        // Given - submission with null values
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setArtifactUrl("https://repo.com");

        // When - simulate @PrePersist
        submission.prePersist();

        // Then
        assertNotNull(submission.getSubmittedAt());
        assertEquals(SubmissionStatus.SUBMITTED, submission.getStatus());
        assertEquals(1, submission.getVersion());
    }

    @Test
    void prePersist_withExistingValues_shouldNotOverride() {
        // Given - submission with existing values
        Instant customTime = Instant.now().minusSeconds(3600);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setArtifactUrl("https://repo.com");
        submission.setSubmittedAt(customTime);
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setVersion(5);

        // When - simulate @PrePersist
        submission.prePersist();

        // Then - existing values should be preserved
        assertEquals(customTime, submission.getSubmittedAt());
        assertEquals(SubmissionStatus.GRADED, submission.getStatus());
        assertEquals(5, submission.getVersion());
    }

    @Test
    void prePersist_withNullStatus_shouldSetDefault() {
        // Given - submission with null status
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setArtifactUrl("https://repo.com");
        submission.setStatus(null);

        // When - simulate @PrePersist
        submission.prePersist();

        // Then
        assertEquals(SubmissionStatus.SUBMITTED, submission.getStatus());
    }

    @Test
    void prePersist_withNullSubmittedAt_shouldSetCurrentTime() {
        // Given - submission with null submittedAt
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setArtifactUrl("https://repo.com");
        submission.setSubmittedAt(null);

        // When - simulate @PrePersist
        submission.prePersist();

        // Then
        assertNotNull(submission.getSubmittedAt());
        assertTrue(submission.getSubmittedAt().isBefore(Instant.now().plusSeconds(1)));
        assertTrue(submission.getSubmittedAt().isAfter(Instant.now().minusSeconds(1)));
    }

    @Test
    void prePersist_withZeroVersion_shouldSetToOne() {
        // Given - submission with zero version
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setArtifactUrl("https://repo.com");
        submission.setVersion(0);

        // When - simulate @PrePersist
        submission.prePersist();

        // Then
        assertEquals(1, submission.getVersion());
    }

    @Test
    void prePersist_withNegativeVersion_shouldSetToOne() {
        // Given - submission with negative version
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setArtifactUrl("https://repo.com");
        submission.setVersion(-5);

        // When - simulate @PrePersist
        submission.prePersist();

        // Then
        assertEquals(1, submission.getVersion());
    }
}

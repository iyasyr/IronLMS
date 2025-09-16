package com.ironhack.lms.domain.enrollment;

import com.ironhack.lms.domain.course.Course;
import com.ironhack.lms.domain.user.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentTest {

    private Enrollment enrollment;
    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        enrollment = new Enrollment();
        student = new Student();
        course = new Course();
    }

    @Test
    void prePersist_shouldSetDefaultValues() {
        // Given - enrollment with null values
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        // When - simulate @PrePersist
        enrollment.prePersist();

        // Then
        assertNotNull(enrollment.getEnrolledAt());
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
    }

    @Test
    void prePersist_withExistingValues_shouldNotOverride() {
        // Given - enrollment with existing values
        Instant customTime = Instant.now().minusSeconds(3600);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(customTime);
        enrollment.setStatus(EnrollmentStatus.CANCELLED);

        // When - simulate @PrePersist
        enrollment.prePersist();

        // Then - existing values should be preserved
        assertEquals(customTime, enrollment.getEnrolledAt());
        assertEquals(EnrollmentStatus.CANCELLED, enrollment.getStatus());
    }

    @Test
    void prePersist_withNullStatus_shouldSetDefault() {
        // Given - enrollment with null status
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(null);

        // When - simulate @PrePersist
        enrollment.prePersist();

        // Then
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
    }

    @Test
    void prePersist_withNullEnrolledAt_shouldSetCurrentTime() {
        // Given - enrollment with null enrolledAt
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(null);

        // When - simulate @PrePersist
        enrollment.prePersist();

        // Then
        assertNotNull(enrollment.getEnrolledAt());
        assertTrue(enrollment.getEnrolledAt().isBefore(Instant.now().plusSeconds(1)));
        assertTrue(enrollment.getEnrolledAt().isAfter(Instant.now().minusSeconds(1)));
    }
}

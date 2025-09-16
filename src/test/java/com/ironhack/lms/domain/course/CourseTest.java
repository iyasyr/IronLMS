package com.ironhack.lms.domain.course;

import com.ironhack.lms.domain.user.Instructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    private Course course;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        course = new Course();
        instructor = new Instructor();
    }

    @Test
    void prePersist_shouldSetDefaultValues() {
        // Given - course with null values
        course.setInstructor(instructor);
        course.setTitle("Test Course");

        // When - simulate @PrePersist
        course.prePersist();

        // Then
        assertNotNull(course.getCreatedAt());
        assertEquals(CourseStatus.DRAFT, course.getStatus());
    }

    @Test
    void prePersist_withExistingValues_shouldNotOverride() {
        // Given - course with existing values
        Instant customTime = Instant.now().minusSeconds(3600);
        course.setInstructor(instructor);
        course.setTitle("Test Course");
        course.setCreatedAt(customTime);
        course.setStatus(CourseStatus.PUBLISHED);

        // When - simulate @PrePersist
        course.prePersist();

        // Then - existing values should be preserved
        assertEquals(customTime, course.getCreatedAt());
        assertEquals(CourseStatus.PUBLISHED, course.getStatus());
    }

    @Test
    void prePersist_withNullStatus_shouldSetDefault() {
        // Given - course with null status
        course.setInstructor(instructor);
        course.setTitle("Test Course");
        course.setStatus(null);

        // When - simulate @PrePersist
        course.prePersist();

        // Then
        assertEquals(CourseStatus.DRAFT, course.getStatus());
    }

    @Test
    void prePersist_withNullCreatedAt_shouldSetCurrentTime() {
        // Given - course with null createdAt
        course.setInstructor(instructor);
        course.setTitle("Test Course");
        course.setCreatedAt(null);

        // When - simulate @PrePersist
        course.prePersist();

        // Then
        assertNotNull(course.getCreatedAt());
        assertTrue(course.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
        assertTrue(course.getCreatedAt().isAfter(Instant.now().minusSeconds(1)));
    }
}

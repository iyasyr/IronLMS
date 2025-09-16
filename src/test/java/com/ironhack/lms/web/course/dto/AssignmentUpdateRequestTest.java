package com.ironhack.lms.web.course.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentUpdateRequestTest {

    @Test
    void constructor_shouldCreateInstance() {
        // Given
        String title = "Updated Assignment";
        String instructions = "Updated instructions";
        Instant dueAt = Instant.now().plusSeconds(86400);
        Integer maxPoints = 150;
        Boolean allowLate = false;

        // When
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(title, instructions, dueAt, maxPoints, allowLate);

        // Then
        assertEquals(title, request.title());
        assertEquals(instructions, request.instructions());
        assertEquals(dueAt, request.dueAt());
        assertEquals(maxPoints, request.maxPoints());
        assertEquals(allowLate, request.allowLate());
    }

    @Test
    void constructor_withNullValues_shouldCreateInstance() {
        // Given
        String title = "Updated Assignment";
        String instructions = "Updated instructions";
        Instant dueAt = null;
        Integer maxPoints = null;
        Boolean allowLate = null;

        // When
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(title, instructions, dueAt, maxPoints, allowLate);

        // Then
        assertEquals(title, request.title());
        assertEquals(instructions, request.instructions());
        assertNull(request.dueAt());
        assertNull(request.maxPoints());
        assertNull(request.allowLate());
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {
        // Given
        Instant dueAt = Instant.now().plusSeconds(86400);
        AssignmentUpdateRequest request1 = new AssignmentUpdateRequest("Title", "Instructions", dueAt, 100, true);
        AssignmentUpdateRequest request2 = new AssignmentUpdateRequest("Title", "Instructions", dueAt, 100, true);

        // When & Then
        assertEquals(request1, request2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues() {
        // Given
        Instant dueAt = Instant.now().plusSeconds(86400);
        AssignmentUpdateRequest request1 = new AssignmentUpdateRequest("Title1", "Instructions", dueAt, 100, true);
        AssignmentUpdateRequest request2 = new AssignmentUpdateRequest("Title2", "Instructions", dueAt, 100, true);

        // When & Then
        assertNotEquals(request1, request2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Given
        Instant dueAt = Instant.now().plusSeconds(86400);
        AssignmentUpdateRequest request = new AssignmentUpdateRequest("Title", "Instructions", dueAt, 100, true);

        // When
        int hashCode1 = request.hashCode();
        int hashCode2 = request.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void toString_shouldContainAllFields() {
        // Given
        Instant dueAt = Instant.now().plusSeconds(86400);
        AssignmentUpdateRequest request = new AssignmentUpdateRequest("Title", "Instructions", dueAt, 100, true);

        // When
        String toString = request.toString();

        // Then
        assertTrue(toString.contains("Title"));
        assertTrue(toString.contains("Instructions"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("true"));
    }
}

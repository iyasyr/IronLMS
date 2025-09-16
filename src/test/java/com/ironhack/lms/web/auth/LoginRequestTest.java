package com.ironhack.lms.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void constructor_shouldCreateInstance() {
        // Given
        String email = "student@lms.local";
        String password = "password";

        // When
        LoginRequest request = new LoginRequest(email, password);

        // Then
        assertEquals(email, request.email());
        assertEquals(password, request.password());
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {
        // Given
        LoginRequest request1 = new LoginRequest("student@lms.local", "password");
        LoginRequest request2 = new LoginRequest("student@lms.local", "password");

        // When & Then
        assertEquals(request1, request2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues() {
        // Given
        LoginRequest request1 = new LoginRequest("student@lms.local", "password");
        LoginRequest request2 = new LoginRequest("instructor@lms.local", "password");

        // When & Then
        assertNotEquals(request1, request2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Given
        LoginRequest request = new LoginRequest("student@lms.local", "password");

        // When
        int hashCode1 = request.hashCode();
        int hashCode2 = request.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void toString_shouldContainEmail() {
        // Given
        LoginRequest request = new LoginRequest("student@lms.local", "password");

        // When
        String toString = request.toString();

        // Then
        assertTrue(toString.contains("student@lms.local"));
    }
}

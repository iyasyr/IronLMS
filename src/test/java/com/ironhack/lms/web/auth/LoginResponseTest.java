package com.ironhack.lms.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void constructor_shouldCreateInstance() {
        // Given
        String token = "jwt-token-123";

        // When
        LoginResponse response = new LoginResponse(token);

        // Then
        assertEquals(token, response.token());
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {
        // Given
        LoginResponse response1 = new LoginResponse("jwt-token-123");
        LoginResponse response2 = new LoginResponse("jwt-token-123");

        // When & Then
        assertEquals(response1, response2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentValues() {
        // Given
        LoginResponse response1 = new LoginResponse("jwt-token-123");
        LoginResponse response2 = new LoginResponse("jwt-token-456");

        // When & Then
        assertNotEquals(response1, response2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Given
        LoginResponse response = new LoginResponse("jwt-token-123");

        // When
        int hashCode1 = response.hashCode();
        int hashCode2 = response.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void toString_shouldContainToken() {
        // Given
        LoginResponse response = new LoginResponse("jwt-token-123");

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("jwt-token-123"));
    }
}

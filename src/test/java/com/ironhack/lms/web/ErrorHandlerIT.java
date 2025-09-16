package com.ironhack.lms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorHandlerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    record Login(String email, String password) {}

    private String login(String email, String password) throws Exception {
        var body = json.writeValueAsString(new Login(email, password));
        var response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    @Test
    void validation_error_when_empty_course_title() throws Exception {
        String instructorToken = login("instructor@lms.local", "password");
        
        // Invalid course request with empty title
        var invalidCourse = """
                { "title": "", "description": "test" }
                """;
        
        mvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCourse))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:validation-error"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void validation_error_when_malformed_json() throws Exception {
        String instructorToken = login("instructor@lms.local", "password");
        
        // Malformed JSON
        String malformedJson = "{ invalid json }";
        
        mvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Malformed JSON"));
    }

    @Test
    void type_mismatch_error_when_invalid_course_id() throws Exception {
        String instructorToken = login("instructor@lms.local", "password");
        
        // Invalid course ID (not a number)
        mvc.perform(get("/api/courses/invalid-id")
                .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Type mismatch"))
                .andExpect(jsonPath("$.error.parameter").value("id"))
                .andExpect(jsonPath("$.error.requiredType").value("Long"));
    }

    @Test
    void unauthorized_error_when_invalid_credentials() throws Exception {
        var invalidLogin = """
                { "email": "invalid@example.com", "password": "wrong" }
                """;
        
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Unauthorized"));
    }

    @Test
    void forbidden_error_when_student_tries_to_create_course() throws Exception {
        String studentToken = login("student@lms.local", "password");
        
        var course = """
                { "title": "Test Course", "description": "test" }
                """;
        
        mvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(course))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Forbidden"));
    }

    @Test
    void not_found_error_when_accessing_nonexistent_course() throws Exception {
        mvc.perform(get("/api/courses/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Course not found"));
    }

    @Test
    void conflict_error_when_duplicate_enrollment() throws Exception {
        String instructorToken = login("instructor@lms.local", "password");
        String studentToken = login("student@lms.local", "password");
        
        // Create and publish a course
        var course = """
                { "title": "Test Course %d", "description": "test" }
                """.formatted(System.currentTimeMillis());
        
        var created = mvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(course))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        long courseId = json.readTree(created).get("id").asLong();
        
        var publish = """
                { "title": "Published Course", "description": "test", "status":"PUBLISHED" }
                """;
        mvc.perform(put("/api/courses/{id}", courseId)
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(publish))
                .andExpect(status().isOk());
        
        // First enrollment succeeds
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
        
        // Second enrollment should fail with conflict
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Already enrolled"));
    }

    @Test
    void bad_request_when_invalid_assignment_data() throws Exception {
        String instructorToken = login("instructor@lms.local", "password");
        
        // Invalid assignment with negative max points
        var invalidAssignment = """
                { "title": "Test", "instructions": "test", "maxPoints": -10, "allowLate": false }
                """;
        
        mvc.perform(post("/api/courses/1/assignments")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidAssignment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:validation-error"));
    }
}

package com.ironhack.lms.web.enrollment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import com.ironhack.lms.service.enrollment.EnrollmentService;
import com.ironhack.lms.web.enrollment.dto.EnrollmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerTest {

    @Mock
    private EnrollmentService service;

    @InjectMocks
    private EnrollmentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestingAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        auth = new TestingAuthenticationToken("student@lms.local", "password");
    }

    @Test
    void enroll_shouldReturnEnrollmentResponse() throws Exception {
        // Given
        EnrollmentResponse response = new EnrollmentResponse(1L, 100L, "Spring Boot 101", 
                EnrollmentStatus.ACTIVE, Instant.now());
        when(service.enroll(eq(100L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/courses/100/enroll")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.courseId").value(100L))
                .andExpect(jsonPath("$.courseTitle").value("Spring Boot 101"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void myEnrollments_shouldReturnPageOfEnrollments() throws Exception {
        // Given
        EnrollmentResponse response = new EnrollmentResponse(1L, 100L, "Spring Boot 101", 
                EnrollmentStatus.ACTIVE, Instant.now());
        Page<EnrollmentResponse> page = new PageImpl<>(List.of(response));
        when(service.myEnrollments(any(), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/enrollments")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].courseTitle").value("Spring Boot 101"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void cancel_shouldReturnUpdatedEnrollment() throws Exception {
        // Given
        EnrollmentResponse response = new EnrollmentResponse(1L, 100L, "Spring Boot 101", 
                EnrollmentStatus.CANCELLED, Instant.now());
        when(service.cancel(eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/enrollments/1/cancel")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void complete_shouldReturnCompletedEnrollment() throws Exception {
        // Given
        EnrollmentResponse response = new EnrollmentResponse(1L, 100L, "Spring Boot 101", 
                EnrollmentStatus.COMPLETED, Instant.now());
        when(service.completeByStaff(eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/enrollments/1/complete")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}

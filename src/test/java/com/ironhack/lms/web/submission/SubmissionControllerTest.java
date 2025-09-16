package com.ironhack.lms.web.submission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.lms.domain.submission.SubmissionStatus;
import com.ironhack.lms.service.submission.SubmissionService;
import com.ironhack.lms.web.submission.dto.*;
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
class SubmissionControllerTest {

    @Mock
    private SubmissionService service;

    @InjectMocks
    private SubmissionController controller;

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
    void submit_shouldReturnSubmissionResponse() throws Exception {
        // Given
        SubmissionResponse response = new SubmissionResponse(1L, 100L, 200L, 300L, 
                Instant.now(), "https://repo.com", SubmissionStatus.SUBMITTED, null, null, 1);
        when(service.submit(eq(100L), any(SubmissionCreateRequest.class), any()))
                .thenReturn(response);

        SubmissionCreateRequest request = new SubmissionCreateRequest("https://repo.com");

        // When & Then
        mockMvc.perform(post("/api/assignments/100/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.assignmentId").value(100L))
                .andExpect(jsonPath("$.artifactUrl").value("https://repo.com"));
    }

    @Test
    void mySubmissions_shouldReturnPageOfSubmissions() throws Exception {
        // Given
        SubmissionResponse response = new SubmissionResponse(1L, 100L, 200L, 300L, 
                Instant.now(), "https://repo.com", SubmissionStatus.SUBMITTED, null, null, 1);
        Page<SubmissionResponse> page = new PageImpl<>(List.of(response));
        when(service.mySubmissions(any(), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/submissions/mine")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listByCourse_shouldReturnPageOfSubmissions() throws Exception {
        // Given
        SubmissionResponse response = new SubmissionResponse(1L, 100L, 200L, 300L, 
                Instant.now(), "https://repo.com", SubmissionStatus.SUBMITTED, null, null, 1);
        Page<SubmissionResponse> page = new PageImpl<>(List.of(response));
        when(service.listByCourse(eq(200L), any(), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/courses/200/submissions")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void grade_shouldReturnUpdatedSubmission() throws Exception {
        // Given
        SubmissionResponse response = new SubmissionResponse(1L, 100L, 200L, 300L, 
                Instant.now(), "https://repo.com", SubmissionStatus.GRADED, 85, "Good work!", 1);
        when(service.grade(eq(1L), any(GradeRequest.class), any())).thenReturn(response);

        GradeRequest request = new GradeRequest(85, "Good work!");

        // When & Then
        mockMvc.perform(patch("/api/submissions/1/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.feedback").value("Good work!"));
    }

    @Test
    void requestResubmission_shouldReturnUpdatedSubmission() throws Exception {
        // Given
        SubmissionResponse response = new SubmissionResponse(1L, 100L, 200L, 300L, 
                Instant.now(), "https://repo.com", SubmissionStatus.RESUBMIT_REQUESTED, null, "Please improve", 1);
        when(service.requestResubmission(eq(1L), any(ResubmitRequest.class), any()))
                .thenReturn(response);

        ResubmitRequest request = new ResubmitRequest("Please improve");

        // When & Then
        mockMvc.perform(patch("/api/submissions/1/request-resubmission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("RESUBMIT_REQUESTED"))
                .andExpect(jsonPath("$.feedback").value("Please improve"));
    }
}

package com.ironhack.lms.web.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.lms.domain.course.CourseStatus;
import com.ironhack.lms.service.course.CourseService;
import com.ironhack.lms.web.course.dto.*;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService service;

    @InjectMocks
    private CourseController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestingAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        auth = new TestingAuthenticationToken("instructor@lms.local", "password");
    }

    @Test
    void listPublished_shouldReturnPageOfCourses() throws Exception {
        // Given
        CourseResponse response = new CourseResponse(1L, 10L, "Spring Boot 101", "Learn Spring Boot", 
                CourseStatus.PUBLISHED, Instant.now(), Instant.now());
        Page<CourseResponse> page = new PageImpl<>(List.of(response));
        when(service.listPublished(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/courses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot 101"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void get_shouldReturnCourse() throws Exception {
        // Given
        CourseResponse response = new CourseResponse(1L, 10L, "Spring Boot 101", "Learn Spring Boot", 
                CourseStatus.PUBLISHED, Instant.now(), Instant.now());
        when(service.getForRead(eq(1L), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/courses/1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Spring Boot 101"));
    }

    @Test
    void create_shouldReturnCreatedCourse() throws Exception {
        // Given
        CourseResponse response = new CourseResponse(1L, 10L, "New Course", "Description", 
                CourseStatus.DRAFT, Instant.now(), null);
        when(service.createCourse(any(CourseCreateRequest.class), any())).thenReturn(response);

        CourseCreateRequest request = new CourseCreateRequest("New Course", "Description");

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Course"));
    }

    @Test
    void update_shouldReturnUpdatedCourse() throws Exception {
        // Given
        CourseResponse response = new CourseResponse(1L, 10L, "Updated Course", "Updated Description", 
                CourseStatus.PUBLISHED, Instant.now(), Instant.now());
        when(service.updateCourse(eq(1L), any(CourseUpdateRequest.class), any())).thenReturn(response);

        CourseUpdateRequest request = new CourseUpdateRequest("Updated Course", "Updated Description", 
                com.ironhack.lms.domain.course.CourseStatus.PUBLISHED);

        // When & Then
        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Course"));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(service).deleteCourse(eq(1L), any());

        // When & Then
        mockMvc.perform(delete("/api/courses/1")
                        .principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void addLesson_shouldReturnLessonId() throws Exception {
        // Given
        when(service.addLesson(eq(1L), any(LessonCreateRequest.class), any())).thenReturn(50L);

        LessonCreateRequest request = new LessonCreateRequest("Lesson 1", "https://video.com", 1);

        // When & Then
        mockMvc.perform(post("/api/courses/1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(50L));
    }

    @Test
    void addAssignment_shouldReturnAssignmentId() throws Exception {
        // Given
        when(service.addAssignment(eq(1L), any(AssignmentCreateRequest.class), any())).thenReturn(60L);

        AssignmentCreateRequest request = new AssignmentCreateRequest("HW1", "Instructions", 
                Instant.now().plusSeconds(86400), 100, true);

        // When & Then
        mockMvc.perform(post("/api/courses/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(60L));
    }

    @Test
    void updateLesson_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(service).updateLesson(eq(1L), eq(50L), any(LessonUpdateRequest.class), any());

        LessonUpdateRequest request = new LessonUpdateRequest("Updated Lesson", "https://new-video.com", 2);

        // When & Then
        mockMvc.perform(put("/api/courses/1/lessons/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLesson_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(service).deleteLesson(eq(1L), eq(50L), any());

        // When & Then
        mockMvc.perform(delete("/api/courses/1/lessons/50")
                        .principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateAssignment_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(service).updateAssignment(eq(1L), eq(60L), any(AssignmentUpdateRequest.class), any());

        AssignmentUpdateRequest request = new AssignmentUpdateRequest("Updated HW", "Updated Instructions", 
                Instant.now().plusSeconds(172800), 150, false);

        // When & Then
        mockMvc.perform(put("/api/courses/1/assignments/60")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAssignment_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(service).deleteAssignment(eq(1L), eq(60L), any());

        // When & Then
        mockMvc.perform(delete("/api/courses/1/assignments/60")
                        .principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void lessons_shouldReturnListOfLessons() throws Exception {
        // Given
        LessonSummaryResponse response = new LessonSummaryResponse(1L, "Lesson 1", "https://video.com", 1);
        when(service.listLessonsForRead(eq(1L), any())).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/courses/1/lessons")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Lesson 1"));
    }

    @Test
    void assignments_shouldReturnListOfAssignments() throws Exception {
        // Given
        AssignmentSummaryResponse response = new AssignmentSummaryResponse(1L, "HW1", "Instructions", 
                100, true, Instant.now().plusSeconds(86400));
        when(service.listAssignmentsForRead(eq(1L), any())).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/courses/1/assignments")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("HW1"));
    }
}

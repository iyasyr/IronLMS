package com.ironhack.lms.web.enrollment;

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

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentIT {
    
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    record Login(String email, String password) {}

    private String login(String email, String password) throws Exception {
        var body = om.writeValueAsString(new Login(email, password));
        var json = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("token").asText();
    }

    private long createPublishedCourse(String instrToken) throws Exception {
        var create = """
                { "title": "Published Course %d", "description": "tmp" }
                """.formatted(System.currentTimeMillis());
        var created = mvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + instrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        var publish = """
                { "title": "Published Course", "description": "tmp", "status":"PUBLISHED" }
                """;
        mvc.perform(put("/api/courses/{id}", id)
                        .header("Authorization", "Bearer " + instrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publish))
                .andExpect(status().isOk());
        return id;
    }

    @Test
    void student_can_enroll_in_published_course() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        var studToken = login("student@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // enroll first time -> 200
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void student_cannot_enroll_twice() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        var studToken = login("student@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // first enrollment -> 200
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk());
        
        // second enrollment -> 409 (conflict)
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isConflict());
    }

    @Test
    void instructor_cannot_enroll() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // instructor cannot enroll -> 403
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + instrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_can_view_own_enrollments() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        var studToken = login("student@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // enroll first
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk());
        
        // view enrollments
        mvc.perform(get("/api/enrollments")
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void instructor_cannot_view_student_enrollments() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        
        // instructor cannot view enrollments -> 403
        mvc.perform(get("/api/enrollments")
                        .header("Authorization", "Bearer " + instrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_can_cancel_own_enrollment() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        var studToken = login("student@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // enroll first
        var enrollResponse = mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        long enrollmentId = om.readTree(enrollResponse).get("id").asLong();
        
        // cancel enrollment
        mvc.perform(patch("/api/enrollments/{id}/cancel", enrollmentId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void instructor_can_complete_enrollment() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        var studToken = login("student@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // student enrolls first
        var enrollResponse = mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        long enrollmentId = om.readTree(enrollResponse).get("id").asLong();
        
        // instructor marks as completed
        mvc.perform(patch("/api/enrollments/{id}/complete", enrollmentId)
                        .header("Authorization", "Bearer " + instrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void student_cannot_complete_enrollment() throws Exception {
        var instrToken = login("instructor@lms.local", "password");
        var studToken = login("student@lms.local", "password");
        
        long courseId = createPublishedCourse(instrToken);
        
        // student enrolls first
        var enrollResponse = mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        long enrollmentId = om.readTree(enrollResponse).get("id").asLong();
        
        // student cannot mark as completed -> 403
        mvc.perform(patch("/api/enrollments/{id}/complete", enrollmentId)
                        .header("Authorization", "Bearer " + studToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_enroll_without_authentication() throws Exception {
        // no auth header -> 401
        mvc.perform(post("/api/courses/1/enroll"))
                .andExpect(status().isUnauthorized());
    }
}

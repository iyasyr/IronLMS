package com.ironhack.lms.web.submission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SubmissionIT {
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    record Login(String email, String password) {
    }

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
                { "title": "IT Course %d", "description": "tmp" }
                """.formatted(System.currentTimeMillis());
        var created = mvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + instrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        var publish = """
                { "title": "Published IT Course", "description": "tmp", "status":"PUBLISHED" }
                """;
        mvc.perform(put("/api/courses/{id}", id)
                        .header("Authorization", "Bearer " + instrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publish))
                .andExpect(status().isOk());
        return id;
    }

    private long createAssignment(long courseId, String instrToken) throws Exception {
        var body = """
                { "title":"HW1", "instructions":"URL", "maxPoints":100, "allowLate":true }
                """;
        var resp = mvc.perform(post("/api/courses/{id}/assignments", courseId)
                        .header("Authorization", "Bearer " + instrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // adjust this depending on what your controller returns:
        // if it returns a DTO:
        try {
            return Long.parseLong(resp);
        } catch (NumberFormatException e) {
            return om.readTree(resp).get("id").asLong();
        }
    }


    @Test
    void student_submits_successfully() throws Exception {
        var instr = login("instructor@lms.local", "password");
        var stud = login("student@lms.local", "password");

        long courseId = createPublishedCourse(instr);
        long assignmentId = createAssignment(courseId, instr);

        // enroll first time -> 200
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk());

        // submit -> 200
        var submit = """
                { "artifactUrl": "https://github.com/user/hw1" }
                """;
        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submit))
                .andExpect(status().isOk()).andDo(print());
    }

    @Test
    void student_submits_and_only_instructor_can_grade() throws Exception {
        var instr = login("instructor@lms.local", "password");
        var stud = login("student@lms.local", "password");

        long courseId = createPublishedCourse(instr);
        long assignmentId = createAssignment(courseId, instr);

        // enroll
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk());

        // submit
        var submit = """
                { "artifactUrl": "https://github.com/user/hw1" }
                """;
        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submit))
                .andExpect(status().isOk()).andDo(print());

        // student cannot grade
        var grade = """
                { "score": 95, "feedback": "ok" }
                """;
        mvc.perform(patch("/api/submissions/1/grade")
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade))
                .andExpect(status().isForbidden());

        // instructor can grade
        mvc.perform(patch("/api/submissions/1/grade")
                        .header("Authorization", "Bearer " + instr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grade))
                .andExpect(status().isOk());
    }

    @Test
    void student_can_view_own_submissions() throws Exception {
        var instr = login("instructor@lms.local", "password");
        var stud = login("student@lms.local", "password");

        long courseId = createPublishedCourse(instr);
        long assignmentId = createAssignment(courseId, instr);

        // enroll and submit
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk());

        var submit = """
                { "artifactUrl": "https://github.com/user/hw1" }
                """;
        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submit))
                .andExpect(status().isOk());

        // check own submissions
        mvc.perform(get("/api/submissions/mine")
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void instructor_can_view_course_submissions() throws Exception {
        var instr = login("instructor@lms.local", "password");
        var stud = login("student@lms.local", "password");

        long courseId = createPublishedCourse(instr);
        long assignmentId = createAssignment(courseId, instr);

        // enroll and submit
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk());

        var submit = """
                { "artifactUrl": "https://github.com/user/hw1" }
                """;
        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submit))
                .andExpect(status().isOk());

        // instructor can see all submissions for course
        mvc.perform(get("/api/courses/{id}/submissions", courseId)
                        .header("Authorization", "Bearer " + instr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void instructor_can_request_resubmission() throws Exception {
        var instr = login("instructor@lms.local", "password");
        var stud = login("student@lms.local", "password");

        long courseId = createPublishedCourse(instr);
        long assignmentId = createAssignment(courseId, instr);

        // enroll and submit
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk());

        var submit = """
                { "artifactUrl": "https://github.com/user/hw1" }
                """;
        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submit))
                .andExpect(status().isOk());

        // instructor requests resubmission
        var resubmit = """
                { "feedback": "Please improve your code" }
                """;
        mvc.perform(patch("/api/submissions/1/request-resubmission")
                        .header("Authorization", "Bearer " + instr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resubmit))
                .andExpect(status().isOk());
    }

    @Test
    void student_cannot_grade_submissions() throws Exception {
        var instr = login("instructor@lms.local", "password");
        var stud = login("student@lms.local", "password");

        long courseId = createPublishedCourse(instr);
        long assignmentId = createAssignment(courseId, instr);

        // enroll and submit
        mvc.perform(post("/api/courses/{id}/enroll", courseId)
                        .header("Authorization", "Bearer " + stud))
                .andExpect(status().isOk());

        var submit = """
                { "artifactUrl": "https://github.com/user/hw1" }
                """;
        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submit))
                .andExpect(status().isOk());

        // student cannot request resubmission
        var resubmit = """
                { "feedback": "Self evaluation" }
                """;
        mvc.perform(patch("/api/submissions/1/request-resubmission")
                        .header("Authorization", "Bearer " + stud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resubmit))
                .andExpect(status().isForbidden());
    }
}

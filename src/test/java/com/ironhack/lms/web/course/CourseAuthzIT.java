package com.ironhack.lms.web.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies: public reads for published, draft hidden to anon,
 * instructor can manage, student can't write.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CourseAuthzIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private String login(String email, String password) throws Exception {
        var body = om.writeValueAsString(new Login(email, password));
        var json = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("token").asText();
    }

    record Login(String email, String password) {
    }

    @Test
    void public_can_list_published_courses_and_nested_content() throws Exception {
        // list published
        mvc.perform(get("/api/courses").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())));

        // pick first course id from list
        var json = mvc.perform(get("/api/courses?page=0&size=1"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(om.readTree(json).get("content").get(0).get("id").asText());

        // course detail is readable
        mvc.perform(get("/api/courses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PUBLISHED")));

        // lessons & assignments endpoints are readable too
        mvc.perform(get("/api/courses/{id}/lessons", id))
                .andExpect(status().isOk());

        mvc.perform(get("/api/courses/{id}/assignments", id))
                .andExpect(status().isOk());
    }

    @Test
    void draft_hidden_to_anon_but_visible_to_owner() throws Exception {
        // login as instructor and create a DRAFT course
        var instr = login("instructor@lms.local", "password");

        var create = """
                {"title":"Draft X","description":"internal"}
                """;
        var created = mvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + instr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long draftId = om.readTree(created).get("id").asLong();

        // anon cannot see draft detail
        mvc.perform(get("/api/courses/{id}", draftId))
                .andExpect(status().isNotFound());

        // owner (instructor) can
        mvc.perform(get("/api/courses/{id}", draftId)
                        .header("Authorization", "Bearer " + instr))
                .andExpect(status().isOk());
    }

    @Test
    void student_cannot_create_course_and_anon_gets_401_on_writes() throws Exception {
        // 1) create draft course payload
        var create = """
                {
                "title": "Draft X",
                "description": "internal"
                }
                """;

        // 2) anon create (use the same or a separate block)
        var payloadNope = """
                {
                  "title": "Nope",
                  "description": "-"
                }
                """;

        mvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNope))
                .andExpect(status().isUnauthorized());

        var student = login("student@lms.local", "password");

        mvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + student)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNope))
                .andExpect(status().isForbidden());

    }
}

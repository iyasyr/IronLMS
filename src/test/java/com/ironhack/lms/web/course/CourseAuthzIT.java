package com.ironhack.lms.web.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseAuthzIT {
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

    @Test
    void public_can_read_published_but_not_draft() throws Exception {
        // list published
        mvc.perform(get("/api/courses?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())));

        // create draft
        var instr = login("instructor@lms.local", "password");
        var create = """
                {
                  "title": "Draft X",
                  "description": "internal"
                }
                """;
        var created = mvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + instr)
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        // anon cannot see draft
        mvc.perform(get("/api/courses/{id}", id))
                .andExpect(status().isNotFound());

        // owner can
        mvc.perform(get("/api/courses/{id}", id)
                        .header("Authorization", "Bearer " + instr))
                .andExpect(status().isOk());
    }

    @Test
    void student_cannot_create_course() throws Exception {
        var student = login("student@lms.local", "password");
        var payload = """
                {
                  "title": "Nope",
                  "description": "-"
                }
                """;

        // anon -> 401
        mvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());

        // student -> 403
        mvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + student)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isForbidden());
    }
}

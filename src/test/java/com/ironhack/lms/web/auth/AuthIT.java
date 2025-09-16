package com.ironhack.lms.web.auth;

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
class AuthIT {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    record Login(String email, String password) {}

    @Test
    void login_success_and_me_works() throws Exception {
        var body = om.writeValueAsString(new Login("student@lms.local","password"));
        var token = om.readTree(mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("token").asText();

        mvc.perform(get("/api/me").header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student@lms.local"));
    }

    @Test
    void login_fail_wrong_password() throws Exception {
        var body = om.writeValueAsString(new Login("student@lms.local","nope"));
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }
}

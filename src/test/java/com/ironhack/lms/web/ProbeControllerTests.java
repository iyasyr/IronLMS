package com.ironhack.lms.web;

import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for /api/ping.
 *
 * Notes:
 * - Uses MockMvc (no real HTTP port needed).
 * - @AutoConfigureTestDatabase swaps your MySQL DataSource with in-memory H2 for tests,
 *   so MySQL doesn’t need to be running.
 * - Because the class name ends with *IT, Maven will only run it if you use the Failsafe plugin
 *   (mvn verify) or you rename it to *Tests to run with Surefire (mvn test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ProbeControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void ping_returnsOk() throws Exception {
        mvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}

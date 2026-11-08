package com.archive.backend.fixtures;

import com.archive.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The "fixtures" profile seeds the database with the e2e data set on startup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("fixtures")
@Import(TestcontainersConfiguration.class)
class FixturesIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void fixtureSoftwareIsServedByTheApi() throws Exception {
        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(hasItems(
                        "Fixture Software Alpha",
                        "Fixture Software Beta",
                        "Fixture Software Gamma"
                )));
    }
}

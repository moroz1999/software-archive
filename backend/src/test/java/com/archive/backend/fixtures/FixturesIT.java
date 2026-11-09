package com.archive.backend.fixtures;

import com.archive.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
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

    /** A fixture country with its 20 cities. */
    private static final String FINLAND = "00000000-0000-0000-0002-000000000006";

    @Autowired
    private MockMvc mvc;

    @Test
    void fixtureSoftwareIsServedByTheApi() throws Exception {
        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(hasItems("Elite", "Manic Miner", "Knight Lore")));
    }

    @Test
    void fixtureCountriesAndCitiesAreServedByTheApi() throws Exception {
        mvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)));
        mvc.perform(get("/api/countries/{id}/cities", FINLAND))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[*].name").value(hasItems("Helsinki", "Tampere")));
    }

    @Test
    void fixtureAuthorsFillMoreThanOneCatalogPage() throws Exception {
        mvc.perform(get("/api/authors").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(100)))
                .andExpect(jsonPath("$.page.totalPages").value(greaterThan(1)));
    }

    @Test
    void fixtureAuthorsCoverLatinCyrillicAndTheRestLetters() throws Exception {
        for (String letter : new String[]{"A", "Б", "#"}) {
            mvc.perform(get("/api/authors").param("letter", letter))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }
    }
}

package com.archive.backend.software.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** The software a person or a group is credited on, as the pages of the person and the group list it. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
@Transactional
@Rollback
class CreditedSoftwareIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID JANE_ALT = id(3);
    private static final UUID CREW = id(4);
    private static final UUID CRW = id(5);
    private static final UUID THE_CREW = id(6);
    private static final UUID ELITE = id(11);
    private static final UUID TETRIS = id(12);
    private static final UUID LINES = id(13);
    private static final UUID PONG = id(14);
    private static final UUID GHOST = id(7);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Jane Doe, known as JD (the display name) and Jane Alt; Crew, known as The Crew (the display name) and CRW.
     * Elite (1985) credits Jane herself and
     * Jane Alt, and Crew itself; Lines (1985) credits JD; Tetris (no date) credits JD and CRW; Pong nobody of them.
     */
    @BeforeEach
    void insertCredits() {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'JD', ?, NULL, true), (?, 'Jane Alt', ?, NULL, false), (?, 'CRW', NULL, ?, false),
                    (?, 'The Crew', NULL, ?, true)
                """, JD, JANE, JANE_ALT, JANE, CRW, CREW, THE_CREW, CREW);
        jdbc.update("""
                INSERT INTO software (id, title, platform_code, release_year) VALUES
                    (?, 'Elite', 'vector', 1985), (?, 'Tetris', 'vector', NULL), (?, 'Lines', 'vector', 1985),
                    (?, 'Pong', 'vector', 1980)
                """, ELITE, TETRIS, LINES, PONG);
        jdbc.update("""
                INSERT INTO software_contribution (software_id, person_id, group_id, label_id, role_codes) VALUES
                    (?, ?, NULL, NULL, ARRAY['code']),
                    (?, NULL, NULL, ?, ARRAY['music']),
                    (?, NULL, ?, NULL, ARRAY['developer']),
                    (?, NULL, NULL, ?, ARRAY['graphics']),
                    (?, NULL, NULL, ?, ARRAY['unknown']),
                    (?, NULL, NULL, ?, ARRAY['publisher'])
                """, ELITE, JANE, ELITE, JANE_ALT, ELITE, CREW, LINES, JD, TETRIS, JD, TETRIS, CRW);
    }

    @Test
    void listsTheSoftwareOfAPersonByYearWithTheirRolesAndTheLabelsOtherThanTheMainName() throws Exception {
        mvc.perform(get("/api/authors/{id}/software", JANE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(contains("Elite", "Lines", "Tetris")))
                .andExpect(jsonPath("$[0].releaseDate").value("1985"))
                .andExpect(jsonPath("$[0].madeBy").isArray())
                .andExpect(jsonPath("$[0].screenshots").isArray())
                .andExpect(jsonPath("$[0].credits[*].as").value(contains(null, "Jane Alt")))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("code")))
                .andExpect(jsonPath("$[0].credits[1].roles").value(contains("music")))
                .andExpect(jsonPath("$[1].credits[0].as").value(nullValue()))
                .andExpect(jsonPath("$[1].credits[0].roles").value(contains("graphics")))
                .andExpect(jsonPath("$[2].credits[0].roles").value(contains("unknown")));
    }

    @Test
    void listsTheSoftwareOfAGroupCreditedByItselfAndByItsLabels() throws Exception {
        mvc.perform(get("/api/groups/{id}/software", CREW))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(contains("Elite", "Tetris")))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("developer")))
                .andExpect(jsonPath("$[1].credits[0].as").value("CRW"))
                .andExpect(jsonPath("$[1].credits[0].roles").value(contains("publisher")));
    }

    @Test
    void listsTheSoftwareOfALabelCreditedUnderItself() throws Exception {
        jdbc.update("INSERT INTO label (id, name) VALUES (?, 'Ghost')", GHOST);
        jdbc.update(
                "INSERT INTO software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['sfx'])",
                PONG,
                GHOST);

        mvc.perform(get("/api/labels/{id}/software", GHOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(contains("Pong")))
                .andExpect(jsonPath("$[0].credits[0].as").value(nullValue()))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("sfx")));
    }

    @Test
    void listsNothingForSomebodyUnknown() throws Exception {
        mvc.perform(get("/api/authors/{id}/software", id(99)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(empty()));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00dd-%012d".formatted(number));
    }
}

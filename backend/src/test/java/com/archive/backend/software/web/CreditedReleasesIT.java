package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.ADMIN_ID;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** The releases a person, a group or a label is credited on, as their pages list them. */
@IntegrationTest
@Transactional
@Rollback
class CreditedReleasesIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID JANE_ALT = id(3);
    private static final UUID CREW = id(4);
    private static final UUID CRW = id(5);
    private static final UUID GHOST = id(6);
    private static final UUID ELITE = id(11);
    private static final UUID TETRIS = id(12);
    private static final UUID ELITE_AY = id(21);
    private static final UUID ELITE_CRACK = id(22);
    private static final UUID TETRIS_ORIGINAL = id(23);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /**
     * Jane Doe, known as JD (the display name) and Jane Alt; Crew, known also as CRW; Ghost, a label of nobody. Elite
     * has the releases Elite AY (1987, version 1.1) and an untitled crack (1990); Tetris an untitled original of no
     * date. Elite AY credits Jane herself; the crack credits JD and CRW; the Tetris original credits Crew itself, Ghost
     * and Jane Alt.
     */
    @BeforeEach
    void insert() throws Exception {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'JD', ?, NULL, true), (?, 'Jane Alt', ?, NULL, false), (?, 'CRW', NULL, ?, false),
                    (?, 'Ghost', NULL, NULL, false)
                """, JD, JANE, JANE_ALT, JANE, CRW, CREW, GHOST);
        jdbc.update("""
                INSERT INTO software (id, title, platform_code, added_by) VALUES
                    (?, 'Elite', 'vector', ?), (?, 'Tetris', 'vector', ?)
                """, ELITE, ADMIN_ID.value(), TETRIS, ADMIN_ID.value());
        jdbc.update(
                """
                INSERT INTO software_release (id, software_id, title, version, type, release_year, added_by) VALUES
                    (?, ?, 'Elite AY', '1.1', 'adaptation', 1987, ?), (?, ?, NULL, NULL, 'crack', 1990, ?),
                    (?, ?, NULL, NULL, 'original', NULL, ?)
                """,
                ELITE_AY,
                ELITE,
                ADMIN_ID.value(),
                ELITE_CRACK,
                ELITE,
                ADMIN_ID.value(),
                TETRIS_ORIGINAL,
                TETRIS,
                ADMIN_ID.value());
        credit(ELITE, ELITE_AY, "Elite AY", "adaptation", """
                {"type": "author", "id": "%s", "roles": ["adaptation"]}
                """.formatted(JANE));
        credit(ELITE, ELITE_CRACK, null, "crack", """
                {"type": "author-label", "id": "%s", "roles": ["release"]},
                {"type": "group-label", "id": "%s", "roles": ["intro_code"]}
                """.formatted(JD, CRW));
        credit(TETRIS, TETRIS_ORIGINAL, null, "original", """
                {"type": "group", "id": "%s", "roles": ["developer"]},
                {"type": "author-label", "id": "%s", "roles": []},
                {"type": "author-label", "id": "%s", "roles": ["music"]}
                """.formatted(CREW, GHOST, JANE_ALT));
    }

    @Test
    void listsTheReleasesOfAPersonByDateWithTheirRolesAndTheLabelsOtherThanTheMainName() throws Exception {
        mvc.perform(get("/api/authors/{id}/releases", JANE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id")
                        .value(contains(ELITE_AY.toString(), ELITE_CRACK.toString(), TETRIS_ORIGINAL.toString())))
                .andExpect(jsonPath("$[0].softwareId").value(ELITE.toString()))
                .andExpect(jsonPath("$[0].softwareTitle").value("Elite"))
                .andExpect(jsonPath("$[0].title").value("Elite AY"))
                .andExpect(jsonPath("$[0].version").value("1.1"))
                .andExpect(jsonPath("$[0].type").value("adaptation"))
                .andExpect(jsonPath("$[0].releaseDate").value("1987"))
                .andExpect(jsonPath("$[0].screenshots").isArray())
                .andExpect(jsonPath("$[0].credits[*].as").value(contains((String) null)))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("adaptation")))
                .andExpect(jsonPath("$[1].title").value("Elite"))
                .andExpect(jsonPath("$[1].version").value(nullValue()))
                .andExpect(jsonPath("$[1].credits[0].as").value(nullValue()))
                .andExpect(jsonPath("$[1].credits[0].roles").value(contains("release")))
                .andExpect(jsonPath("$[2].credits[*].as").value(contains("Jane Alt")))
                .andExpect(jsonPath("$[2].credits[0].roles").value(contains("music")));
    }

    @Test
    void listsTheReleasesOfAGroupCreditedByItselfAndByItsLabels() throws Exception {
        mvc.perform(get("/api/groups/{id}/releases", CREW))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(ELITE_CRACK.toString(), TETRIS_ORIGINAL.toString())))
                .andExpect(jsonPath("$[0].credits[0].as").value("CRW"))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("intro_code")))
                .andExpect(jsonPath("$[1].title").value("Tetris"))
                .andExpect(jsonPath("$[1].releaseDate").value(nullValue()))
                .andExpect(jsonPath("$[1].credits[0].as").value(nullValue()))
                .andExpect(jsonPath("$[1].credits[0].roles").value(contains("developer")));
    }

    @Test
    void listsTheReleasesOfALabelCreditedUnderItself() throws Exception {
        mvc.perform(get("/api/labels/{id}/releases", GHOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(TETRIS_ORIGINAL.toString())))
                .andExpect(jsonPath("$[0].credits[0].as").value(nullValue()))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("unknown")));
    }

    @Test
    void aReleaseWithoutScreenshotsOfItsOwnShowsThoseOfItsSoftware() throws Exception {
        UUID ofElite = id(31);
        UUID ofEliteAy = id(32);
        jdbc.update(
                "INSERT INTO software_screenshot (software_id, position, id, format, file_name, size) VALUES (?, 0, ?, 'png', 'shot.png', 1)",
                ELITE,
                ofElite);
        jdbc.update(
                "INSERT INTO software_release_screenshot (release_id, position, id, format, file_name, size) VALUES (?, 0, ?, 'png', 'shot.png', 1)",
                ELITE_AY,
                ofEliteAy);

        mvc.perform(get("/api/authors/{id}/releases", JANE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id")
                        .value(contains(ELITE_AY.toString(), ELITE_CRACK.toString(), TETRIS_ORIGINAL.toString())))
                .andExpect(jsonPath("$[0].screenshots[*].id").value(contains(ofEliteAy.toString())))
                .andExpect(jsonPath("$[1].screenshots[*].id").value(contains(ofElite.toString())))
                .andExpect(jsonPath("$[2].screenshots").value(empty()));
    }

    @Test
    void listsNothingForSomebodyUnknown() throws Exception {
        mvc.perform(get("/api/authors/{id}/releases", id(99)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(empty()));
    }

    @Test
    void leavesTheSoftwareCreditedOnlyOnItsReleasesOutOfTheSoftwareOfThePerson() throws Exception {
        mvc.perform(get("/api/authors/{id}/software", JANE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(empty()));
    }

    /** Saves the release as inserted, with the credits. */
    private void credit(UUID software, UUID release, @Nullable String title, String type, String credits)
            throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", software, release)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"title": %s, "version": %s, "type": "%s", "releaseDate": %s, "credits": [%s]}
                                """.formatted(
                                        title == null ? "null" : "\"" + title + "\"",
                                        release.equals(ELITE_AY) ? "\"1.1\"" : "null",
                                        type,
                                        release.equals(ELITE_AY)
                                                ? "\"1987\""
                                                : release.equals(ELITE_CRACK) ? "\"1990\"" : "null",
                                        credits)))
                .andExpect(status().isNoContent());
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00b0-%012d".formatted(number));
    }
}

package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.ADMIN_ID;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Software and a release credit an owner once: a person or a group credited by themselves and under a label of
 * theirs keeps only the credit under the label, with the roles of both (docs/domain/software.md#credits).
 */
@IntegrationTest
@Transactional
@Rollback
class OwnerAndLabelCreditedOnceIT {

    private static final UUID ALEKSEY = id(1);
    private static final UUID KUZNETSOV_A = id(2);
    private static final UUID LYOSHA = id(3);
    private static final UUID BORODA = id(4);
    private static final UUID CREW = id(5);
    private static final UUID CRW = id(6);
    private static final UUID ELITE = id(11);
    private static final UUID ELITE_AY = id(12);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /**
     * Алексей Кузнецов, known as Кузнецов А. (the display name), Lyosha and Boroda; Crew, known also as CRW; Elite,
     * with the release Elite AY.
     */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Алексей Кузнецов')", ALEKSEY);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'Кузнецов А.', ?, NULL, true), (?, 'Lyosha', ?, NULL, false), (?, 'Boroda', ?, NULL, false),
                    (?, 'CRW', NULL, ?, false)
                """, KUZNETSOV_A, ALEKSEY, LYOSHA, ALEKSEY, BORODA, ALEKSEY, CRW, CREW);
        jdbc.update(
                "INSERT INTO software (id, title, platform_code, added_by) VALUES (?, 'Elite', 'vector', ?)",
                ELITE,
                ADMIN_ID.value());
        jdbc.update(
                "INSERT INTO software_release (id, software_id, title, type, added_by) VALUES (?, ?, 'Elite AY', 'original', ?)",
                ELITE_AY,
                ELITE,
                ADMIN_ID.value());
    }

    @Test
    void savingSoftwareJoinsThePersonIntoTheCreditOfTheirLabel() throws Exception {
        saveSoftware(credit("author", ALEKSEY, "code") + "," + credit("author-label", LYOSHA, "unknown"));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(LYOSHA.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
    }

    @Test
    void savingSoftwareJoinsTheRolesOfThePersonAndOfTheirLabel() throws Exception {
        saveSoftware(credit("author", ALEKSEY, "code", "music") + "," + credit("author-label", LYOSHA, "graphics"));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(LYOSHA.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(containsInAnyOrder("code", "music", "graphics")));
    }

    @Test
    void savingSoftwareJoinsThePersonIntoTheirDisplayNameAmongSeveralLabels() throws Exception {
        saveSoftware(credit("author", ALEKSEY, "code")
                + "," + credit("author-label", LYOSHA, "music")
                + "," + credit("author-label", KUZNETSOV_A, "graphics"));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(KUZNETSOV_A.toString(), LYOSHA.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(KUZNETSOV_A))
                        .value(containsInAnyOrder("graphics", "code")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(LYOSHA))
                        .value(contains("music")));
    }

    @Test
    void savingSoftwareJoinsThePersonIntoTheFirstLabelByNameWithoutTheDisplayName() throws Exception {
        saveSoftware(credit("author", ALEKSEY, "code")
                + "," + credit("author-label", LYOSHA, "music")
                + "," + credit("author-label", BORODA, "graphics"));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(containsInAnyOrder(BORODA.toString(), LYOSHA.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(BORODA))
                        .value(containsInAnyOrder("graphics", "code")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(LYOSHA))
                        .value(contains("music")));
    }

    @Test
    void savingSoftwareJoinsTheGroupIntoTheCreditOfItsLabel() throws Exception {
        saveSoftware(credit("group", CREW, "developer") + "," + credit("group-label", CRW, "publisher"));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(contains(CRW.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(containsInAnyOrder("developer", "publisher")));
    }

    @Test
    void savingSoftwareKeepsAPersonCreditedOnlyByThemselves() throws Exception {
        saveSoftware(credit("author", ALEKSEY, "code") + "," + credit("group-label", CRW, "publisher"));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id").value(containsInAnyOrder(ALEKSEY.toString(), CRW.toString())));
    }

    @Test
    void addingSoftwareJoinsThePersonIntoTheCreditOfTheirLabel() throws Exception {
        String created = mvc.perform(post("/api/software")
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"title": "Tetris", "platform": "vector", "credits": [%s]}
                                """.formatted(credit("author", ALEKSEY, "code") + ","
                                + credit("author-label", LYOSHA, "unknown"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mvc.perform(get("/api/software/{id}", (String) JsonPath.read(created, "$.id")))
                .andExpect(jsonPath("$.credits[*].id").value(contains(LYOSHA.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("code")));
    }

    @Test
    void savingAReleaseJoinsThePersonIntoTheCreditOfTheirLabel() throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(release(
                                credit("author", ALEKSEY, "code") + "," + credit("author-label", LYOSHA, "music"))))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY))
                .andExpect(jsonPath("$.credits[*].id").value(contains(LYOSHA.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(containsInAnyOrder("code", "music")));
    }

    @Test
    void addingAReleaseJoinsTheGroupIntoTheCreditOfItsLabel() throws Exception {
        String created = mvc.perform(post("/api/software/{id}/releases", ELITE)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(release(
                                credit("group", CREW, "release") + "," + credit("group-label", CRW, "unknown"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, (String) JsonPath.read(created, "$.id")))
                .andExpect(jsonPath("$.credits[*].id").value(contains(CRW.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("release")));
    }

    private void saveSoftware(String credits) throws Exception {
        mvc.perform(put("/api/software/{id}", ELITE)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"title": "Elite", "platform": "vector", "credits": [%s]}
                                """.formatted(credits)))
                .andExpect(status().isNoContent());
    }

    private static String release(String credits) {
        return """
                {"title": "Elite AY", "type": "original", "credits": [%s]}
                """.formatted(credits);
    }

    private static String credit(String type, UUID id, String... roles) {
        return """
                {"type": "%s", "id": "%s", "roles": [%s]}
                """.formatted(
                        type,
                        id,
                        String.join(
                                ",",
                                java.util.Arrays.stream(roles)
                                        .map(role -> "\"" + role + "\"")
                                        .toList()));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ac-%012d".formatted(number));
    }
}

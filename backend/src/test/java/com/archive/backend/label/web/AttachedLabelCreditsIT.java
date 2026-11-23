package com.archive.backend.label.web;

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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * A label attached to its owner takes in the owner's own credit on the software and the releases crediting both, with
 * the roles of both, so that nobody is credited twice (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class AttachedLabelCreditsIT {

    private static final UUID ALEKSEY = id(1);
    private static final UUID KUZNETSOV_ALEKSEY = id(2);
    private static final UUID KUZNETSOV_A = id(3);
    private static final UUID CREW = id(4);
    private static final UUID OTHER_CREW = id(5);
    private static final UUID CRW = id(6);
    private static final UUID ELITE = id(11);
    private static final UUID ELITE_AY = id(12);
    private static final UUID TETRIS = id(13);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /**
     * Алексей Кузнецов, known as Кузнецов Алексей; Кузнецов А., a label of nobody; the groups Crew and Other Crew; CRW,
     * a label of nobody. Elite credits Алексей himself (code) and Кузнецов А. (unknown), Crew itself (developer) and
     * CRW (publisher); its release Elite AY credits Алексей himself (release) and Кузнецов А. (intro_code), Crew itself
     * (release) and CRW (intro_music). Tetris credits only Алексей himself.
     */
    @BeforeEach
    void insert() throws Exception {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Алексей Кузнецов')", ALEKSEY);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew'), (?, 'Other Crew')", CREW, OTHER_CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, is_display_name) VALUES
                    (?, 'Кузнецов Алексей', ?, true), (?, 'Кузнецов А.', NULL, false), (?, 'CRW', NULL, false)
                """, KUZNETSOV_ALEKSEY, ALEKSEY, KUZNETSOV_A, CRW);
        jdbc.update("""
                INSERT INTO software (id, title, platform_code, added_by) VALUES
                    (?, 'Elite', 'vector', ?), (?, 'Tetris', 'vector', ?)
                """, ELITE, ADMIN_ID.value(), TETRIS, ADMIN_ID.value());
        jdbc.update("""
                INSERT INTO software_contribution (software_id, person_id, group_id, label_id, role_codes) VALUES
                    (?, ?, NULL, NULL, ARRAY['code']), (?, NULL, NULL, ?, ARRAY['unknown']),
                    (?, NULL, ?, NULL, ARRAY['developer']), (?, NULL, NULL, ?, ARRAY['publisher']),
                    (?, ?, NULL, NULL, ARRAY['music'])
                """, ELITE, ALEKSEY, ELITE, KUZNETSOV_A, ELITE, CREW, ELITE, CRW, TETRIS, ALEKSEY);
        jdbc.update(
                "INSERT INTO software_release (id, software_id, title, type, added_by) VALUES (?, ?, 'Elite AY', 'original', ?)",
                ELITE_AY,
                ELITE,
                ADMIN_ID.value());
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Elite AY",
                                  "type": "original",
                                  "credits": [
                                    {"type": "author", "id": "%s", "roles": ["release"]},
                                    {"type": "author-label", "id": "%s", "roles": ["intro_code"]},
                                    {"type": "group", "id": "%s", "roles": ["release"]},
                                    {"type": "author-label", "id": "%s", "roles": ["intro_music"]}
                                  ]
                                }
                                """.formatted(ALEKSEY, KUZNETSOV_A, CREW, CRW)))
                .andExpect(status().isNoContent());
    }

    @Test
    void attachingALabelToAPersonJoinsTheirOwnCreditsIntoThoseOfTheLabel() throws Exception {
        mvc.perform(post("/api/labels/{id}/owner", KUZNETSOV_A)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"type": "author", "id": "%s", "display": false}
                                """.formatted(ALEKSEY)))
                .andExpect(status().isNoContent());

        expectPersonJoinedIntoTheLabel();
    }

    @Test
    void attachingALabelToAGroupJoinsItsOwnCreditsIntoThoseOfTheLabel() throws Exception {
        mvc.perform(post("/api/labels/{id}/owner", CRW)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"type": "group", "id": "%s"}
                                """.formatted(CREW)))
                .andExpect(status().isNoContent());

        expectGroupJoinedIntoTheLabel();
    }

    @Test
    void choosingThePersonAsTheOwnerInTheLabelFormJoinsTheirOwnCreditsIntoThoseOfTheLabel() throws Exception {
        mvc.perform(put("/api/labels/{id}", KUZNETSOV_A)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"name": "Кузнецов А.", "owner": {"type": "author", "id": "%s", "display": false}, "locations": []}
                                """.formatted(ALEKSEY)))
                .andExpect(status().isNoContent());

        expectPersonJoinedIntoTheLabel();
    }

    @Test
    void makingALabelALabelOfTheGroupJoinsItsOwnCreditsIntoThoseOfTheLabel() throws Exception {
        jdbc.update("UPDATE label SET group_id = ? WHERE id = ?", OTHER_CREW, CRW);

        mvc.perform(post("/api/labels/{id}/attach-to-group", CRW)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"group": "%s"}
                                """.formatted(CREW)))
                .andExpect(status().isNoContent());

        expectGroupJoinedIntoTheLabel();
    }

    @Test
    void attachingALabelLeavesThePersonCreditedOnSoftwareWithoutTheLabel() throws Exception {
        mvc.perform(post("/api/labels/{id}/owner", KUZNETSOV_A)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"type": "author", "id": "%s", "display": false}
                                """.formatted(ALEKSEY)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}", TETRIS))
                .andExpect(jsonPath("$.credits[*].id").value(contains(ALEKSEY.toString())))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("music")));
    }

    private void expectPersonJoinedIntoTheLabel() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(KUZNETSOV_A.toString(), CREW.toString(), CRW.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(KUZNETSOV_A))
                        .value(contains("code")));
        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY))
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(KUZNETSOV_A.toString(), CREW.toString(), CRW.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(KUZNETSOV_A))
                        .value(containsInAnyOrder("intro_code", "release")));
    }

    private void expectGroupJoinedIntoTheLabel() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(ALEKSEY.toString(), KUZNETSOV_A.toString(), CRW.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(CRW))
                        .value(containsInAnyOrder("publisher", "developer")));
        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY))
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(ALEKSEY.toString(), KUZNETSOV_A.toString(), CRW.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(CRW))
                        .value(containsInAnyOrder("intro_music", "release")));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ae-%012d".formatted(number));
    }
}

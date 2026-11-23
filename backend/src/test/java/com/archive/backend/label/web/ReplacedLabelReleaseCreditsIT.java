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
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * A label that ceases to exist — made into a person or a group, or merged — hands its credits on releases to whoever
 * replaces it, joined with the credit they already have there (docs/domain/authors.md#label).
 */
@IntegrationTest
@Transactional
@Rollback
class ReplacedLabelReleaseCreditsIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(3);
    private static final UUID GHOST = id(4);
    private static final UUID SPOOK = id(5);
    private static final UUID ELITE = id(11);
    private static final UUID ELITE_AY = id(12);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /**
     * Jane Doe, known as JD; Crew; Ghost and Spook, labels of nobody. The release Elite AY of Elite credits Ghost
     * (code), Jane herself (unknown), Crew (release) and Spook (music).
     */
    @BeforeEach
    void insert() throws Exception {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, is_display_name) VALUES
                    (?, 'JD', ?, true), (?, 'Ghost', NULL, false), (?, 'Spook', NULL, false)
                """, JD, JANE, GHOST, SPOOK);
        jdbc.update(
                "INSERT INTO software (id, title, platform_code, added_by) VALUES (?, 'Elite', 'vector', ?)",
                ELITE,
                ADMIN_ID.value());
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
                                    {"type": "author-label", "id": "%s", "roles": ["code"]},
                                    {"type": "author", "id": "%s", "roles": ["unknown"]},
                                    {"type": "group", "id": "%s", "roles": ["release"]},
                                    {"type": "author-label", "id": "%s", "roles": ["music"]}
                                  ]
                                }
                                """.formatted(GHOST, JANE, CREW, SPOOK)))
                .andExpect(status().isNoContent());
    }

    @Test
    void aPersonMadeOfTheLabelIsCreditedOnTheReleaseInsteadOfIt() throws Exception {
        String made = mvc.perform(admin(post("/api/labels/{id}/author", GHOST)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String person = JsonPath.read(made, "$.id");

        releaseCredits()
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(person, JANE.toString(), CREW.toString(), SPOOK.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(person))
                        .value(contains("code")));
    }

    @Test
    void aGroupMadeOfTheLabelIsCreditedOnTheReleaseInsteadOfIt() throws Exception {
        String made = mvc.perform(admin(post("/api/labels/{id}/group", GHOST))
                        .contentType("application/json")
                        .content("{\"types\": []}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String group = JsonPath.read(made, "$.id");

        releaseCredits()
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(group, JANE.toString(), CREW.toString(), SPOOK.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(group))
                        .value(contains("code")));
    }

    @Test
    void mergingTheLabelIntoAGroupJoinsItsReleaseCreditWithTheGroups() throws Exception {
        mvc.perform(admin(post("/api/labels/{id}/merge-into-group", GHOST))
                        .contentType("application/json")
                        .content("{\"group\": \"%s\"}".formatted(CREW)))
                .andExpect(status().isNoContent());

        releaseCredits()
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(JANE.toString(), CREW.toString(), SPOOK.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(CREW))
                        .value(containsInAnyOrder("release", "code")));
    }

    @Test
    void mergingTheLabelIntoAPersonJoinsItsReleaseCreditWithThePersons() throws Exception {
        mvc.perform(admin(post("/api/labels/{id}/merge-into-author", GHOST))
                        .contentType("application/json")
                        .content("{\"author\": \"%s\"}".formatted(JANE)))
                .andExpect(status().isNoContent());

        releaseCredits()
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(JANE.toString(), CREW.toString(), SPOOK.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(JANE))
                        .value(contains("code")));
    }

    @Test
    void mergingTheLabelIntoAnotherLabelJoinsItsReleaseCreditWithTheOthers() throws Exception {
        mvc.perform(admin(post("/api/labels/{id}/merge-into-label", GHOST))
                        .contentType("application/json")
                        .content("{\"label\": \"%s\"}".formatted(SPOOK)))
                .andExpect(status().isNoContent());

        releaseCredits()
                .andExpect(jsonPath("$.credits[*].id")
                        .value(containsInAnyOrder(JANE.toString(), CREW.toString(), SPOOK.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(SPOOK))
                        .value(containsInAnyOrder("music", "code")));
    }

    private MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder request) {
        return request.with(TestActors.bearer(accessTokens, ADMIN));
    }

    private ResultActions releaseCredits() throws Exception {
        return mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY));
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00af-%012d".formatted(number));
    }
}

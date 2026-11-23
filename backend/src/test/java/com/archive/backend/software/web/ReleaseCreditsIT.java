package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.ADMIN_ID;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

/** Credits of people, groups and their labels on a release, as on software (docs/domain/software.md#releases). */
@IntegrationTest
@Transactional
@Rollback
class ReleaseCreditsIT {

    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(3);
    private static final UUID CRW = id(4);
    private static final UUID GHOST = id(5);
    private static final UUID ELITE = id(11);
    private static final UUID ELITE_AY = id(12);
    private static final UUID UNKNOWN = id(99);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Jane Doe, known as JD; Crew, known also as CRW; Ghost, a label of nobody; Elite with the release Elite AY. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'JD', ?, NULL, true), (?, 'CRW', NULL, ?, false), (?, 'Ghost', NULL, NULL, false)
                """, JD, JANE, CRW, CREW, GHOST);
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
    void addsAReleaseWithPeopleGroupsAndLabelsCreditedWithTheirRoles() throws Exception {
        String created = mvc.perform(post("/api/software/{id}/releases", ELITE)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {
                                  "type": "crack",
                                  "credits": [
                                    {"type": "author", "id": "%s", "roles": ["release"]},
                                    {"type": "group-label", "id": "%s", "roles": ["release", "intro_code"]},
                                    {"type": "author-label", "id": "%s", "roles": []}
                                  ]
                                }
                                """.formatted(JANE, CRW, GHOST)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, (String) JsonPath.read(created, "$.id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits[*].type")
                        .value(containsInAnyOrder("author", "group-label", "author-label")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].title".formatted(JANE))
                        .value(contains("JD")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].ownerId".formatted(JANE))
                        .value(contains(JANE.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(JANE))
                        .value(contains("release")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].title".formatted(CRW))
                        .value(contains("CRW — Crew")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].ownerId".formatted(CRW))
                        .value(contains(CREW.toString())))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(CRW))
                        .value(contains("release", "intro_code")))
                .andExpect(jsonPath("$.credits[?(@.id == '%s')].roles[*]".formatted(GHOST))
                        .value(contains("unknown")));
    }

    @Test
    void replacesTheCreditsOfAReleaseWhenItIsSaved() throws Exception {
        saveRelease("""
                {"type": "author", "id": "%s", "roles": ["code"]}, {"type": "group", "id": "%s", "roles": ["release"]}
                """.formatted(JANE, CREW));
        saveRelease("""
                {"type": "group", "id": "%s", "roles": ["release", "intro_music"]}
                """.formatted(CREW));

        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY))
                .andExpect(jsonPath("$.credits[*].id").value(contains(CREW.toString())))
                .andExpect(jsonPath("$.credits[0].type").value("group"))
                .andExpect(jsonPath("$.credits[0].title").value("Crew"))
                .andExpect(jsonPath("$.credits[0].roles").value(contains("release", "intro_music")));
    }

    @Test
    void listsTheCreditsOfEveryReleaseOfTheSoftware() throws Exception {
        saveRelease("""
                {"type": "author-label", "id": "%s", "roles": ["code"]}
                """.formatted(JD));

        mvc.perform(get("/api/software/{id}/releases", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].credits[*].id").value(contains(JD.toString())))
                .andExpect(jsonPath("$[0].credits[0].type").value("author-label"))
                .andExpect(jsonPath("$[0].credits[0].ownerId").value(JANE.toString()))
                .andExpect(jsonPath("$[0].credits[0].roles").value(contains("code")));
    }

    @Test
    void leavesTheCreditsOfTheSoftwareApartFromThoseOfItsRelease() throws Exception {
        saveRelease("""
                {"type": "group", "id": "%s", "roles": ["release"]}
                """.formatted(CREW));

        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(jsonPath("$.credits").value(empty()));
        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY))
                .andExpect(jsonPath("$.credits[*].id").value(contains(CREW.toString())));
    }

    @Test
    void deletingALabelDeletesItsCreditsOnReleases() throws Exception {
        saveRelease("""
                {"type": "author-label", "id": "%s", "roles": ["code"]}, {"type": "group", "id": "%s", "roles": ["release"]}
                """.formatted(GHOST, CREW));

        mvc.perform(delete("/api/labels/{id}", GHOST).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY))
                .andExpect(jsonPath("$.credits[*].id").value(contains(CREW.toString())));
    }

    @Test
    void refusesACreditedParticipantThatIsNotInTheArchive() throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(release("""
                                {"type": "author", "id": "%s", "roles": ["code"]}
                                """.formatted(UNKNOWN))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("software.credit-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("credits"));
    }

    @Test
    void refusesAParticipantCreditedTwice() throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(release("""
                                {"type": "group", "id": "%1$s", "roles": ["release"]},
                                {"type": "group", "id": "%1$s", "roles": ["code"]}
                                """.formatted(CREW))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("software.credit-duplicate"));
    }

    @Test
    void refusesARoleThatIsNotInTheList() throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(release("""
                                {"type": "group", "id": "%s", "roles": ["juggling"]}
                                """.formatted(CREW))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("software.credit-role-invalid"));
    }

    private void saveRelease(String credits) throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", ELITE, ELITE_AY)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content(release(credits)))
                .andExpect(status().isNoContent());
    }

    private static String release(String credits) {
        return """
                {"title": "Elite AY", "type": "original", "credits": [%s]}
                """.formatted(credits);
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00ad-%012d".formatted(number));
    }
}

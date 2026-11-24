package com.archive.backend.assistant.web.entry;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.ADMIN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.DocumentContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** The pages of entries as the assistant reads them: the entry with what its page lists. */
@IntegrationTest
@Transactional
@Rollback
class EntryToolsIT {

    private static final ToolContext CONTEXT = new ToolContext(TestActors.ADMIN, Language.EN);
    private static final UUID JANE = id(1);
    private static final UUID JD = id(2);
    private static final UUID CREW = id(4);
    private static final UUID CRW = id(5);
    private static final UUID GHOST = id(6);
    private static final UUID ELITE = id(11);
    private static final UUID TETRIS = id(12);
    private static final UUID ELITE_AY = id(21);
    private static final UUID ELITE_CRACK = id(22);
    private static final UUID TETRIS_ORIGINAL = id(23);

    @Autowired
    private ToolCalls calls;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /**
     * Jane Doe, known as JD; Crew, known also as CRW; Ghost, a label of nobody. Elite has Elite AY, credited to Jane,
     * and an untitled crack, credited to JD and CRW; Tetris an original credited to Crew and Ghost.
     */
    @BeforeEach
    void insert() throws Exception {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'JD', ?, NULL, true), (?, 'CRW', NULL, ?, false), (?, 'Ghost', NULL, NULL, false)
                """, JD, JANE, CRW, CREW, GHOST);
        jdbc.update("""
                INSERT INTO software (id, title, platform_code, added_by) VALUES
                    (?, 'Elite', 'vector', ?), (?, 'Tetris', 'vector', ?)
                """, ELITE, ADMIN_ID.value(), TETRIS, ADMIN_ID.value());
        jdbc.update(
                """
                INSERT INTO software_release (id, software_id, title, type, release_year, added_by) VALUES
                    (?, ?, 'Elite AY', 'adaptation', 1987, ?), (?, ?, NULL, 'crack', 1990, ?),
                    (?, ?, NULL, 'original', NULL, ?)
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
        credit(ELITE, ELITE_AY, "\"Elite AY\"", "adaptation", """
                {"type": "author", "id": "%s", "roles": ["adaptation"]}
                """.formatted(JANE));
        credit(ELITE, ELITE_CRACK, "null", "crack", """
                {"type": "author-label", "id": "%s", "roles": ["release"]},
                {"type": "group-label", "id": "%s", "roles": ["intro_code"]}
                """.formatted(JD, CRW));
        credit(TETRIS, TETRIS_ORIGINAL, "null", "original", """
                {"type": "group", "id": "%s", "roles": ["developer"]},
                {"type": "author-label", "id": "%s", "roles": ["music"]}
                """.formatted(CREW, GHOST));
    }

    @Test
    void readsSoftwareWithItsScreenshotsAndReleases() {
        DocumentContext json = read("get_software", "{\"id\": \"%s\"}".formatted(ELITE));

        assertThat(json.read("$.software.title", String.class)).isEqualTo("Elite");
        assertThat(ToolJson.list(json, "$.screenshots")).isEmpty();
        assertThat(ToolJson.list(json, "$.releases[*].id"))
                .containsExactlyInAnyOrder(ELITE_AY.toString(), ELITE_CRACK.toString());
    }

    @Test
    void readsAReleaseWithItsCreditsScreenshotsAndFiles() {
        DocumentContext json =
                read("get_release", "{\"softwareId\": \"%s\", \"releaseId\": \"%s\"}".formatted(ELITE, ELITE_AY));

        assertThat(json.read("$.release.title", String.class)).isEqualTo("Elite AY");
        assertThat(json.read("$.release.credits[0].id", String.class)).isEqualTo(JANE.toString());
        assertThat(ToolJson.list(json, "$.screenshots")).isEmpty();
        assertThat(json.read("$.files", Object.class)).isNull();
    }

    @Test
    void readsAPersonWithWhatTheyAreCreditedOnUnderAnyName() {
        DocumentContext json = read("get_author", "{\"id\": \"%s\"}".formatted(JANE));

        assertThat(json.read("$.author.realName", String.class)).isEqualTo("Jane Doe");
        assertThat(ToolJson.list(json, "$.author.labels[*].name")).containsExactly("JD");
        assertThat(ToolJson.list(json, "$.software")).isEmpty();
        assertThat(ToolJson.list(json, "$.releases[*].id"))
                .containsExactlyInAnyOrder(ELITE_AY.toString(), ELITE_CRACK.toString());
    }

    @Test
    void readsAGroupWithWhatItIsCreditedOnUnderAnyName() {
        DocumentContext json = read("get_group", "{\"id\": \"%s\"}".formatted(CREW));

        assertThat(json.read("$.group.name", String.class)).isEqualTo("Crew");
        assertThat(ToolJson.list(json, "$.group.labels[*].name")).containsExactly("CRW");
        assertThat(ToolJson.list(json, "$.releases[*].id"))
                .containsExactlyInAnyOrder(ELITE_CRACK.toString(), TETRIS_ORIGINAL.toString());
    }

    @Test
    void readsALabelWithWhatIsCreditedUnderIt() {
        DocumentContext json = read("get_label", "{\"id\": \"%s\"}".formatted(GHOST));

        assertThat(json.read("$.label.name", String.class)).isEqualTo("Ghost");
        assertThat(json.read("$.label.owner", Object.class)).isNull();
        assertThat(ToolJson.list(json, "$.releases[*].id")).containsExactly(TETRIS_ORIGINAL.toString());
    }

    @Test
    void refusesAnIdOfNothing() {
        assertThatThrownBy(() -> calls.read("get_author", "{\"id\": \"%s\"}".formatted(id(99)), CONTEXT))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> calls.read(
                        "get_release",
                        "{\"softwareId\": \"%s\", \"releaseId\": \"%s\"}".formatted(TETRIS, ELITE_AY),
                        CONTEXT))
                .isInstanceOf(NotFoundException.class);
    }

    private DocumentContext read(String tool, String arguments) {
        return ToolJson.of(calls.read(tool, arguments, CONTEXT));
    }

    private void credit(UUID software, UUID release, String title, String type, String credits) throws Exception {
        mvc.perform(put("/api/software/{id}/releases/{releaseId}", software, release)
                        .with(TestActors.bearer(accessTokens, ADMIN))
                        .contentType("application/json")
                        .content("""
                                {"title": %s, "version": null, "type": "%s", "releaseDate": null, "credits": [%s]}
                                """.formatted(title, type, credits)))
                .andExpect(status().isNoContent());
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}

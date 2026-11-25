package com.archive.backend.assistant.web.software;

import static com.archive.backend.assistant.web.tool.WriteToolsTestData.CREW;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.GAMES;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.JANE;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.JD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.assistant.web.tool.WriteToolsTestData;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.BusinessException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** The changes of software a plan makes: as the editor, through the use cases of the software form. */
@IntegrationTest
@Transactional
@Rollback
class SoftwareWriteToolsIT {

    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);
    private static final ToolContext OTHER = new ToolContext(TestActors.OTHER, Language.EN);
    private static final String PAGE = "https://www.zxart.ee/eng/software/game/elite/";
    private static final String OTHER_PAGE = "https://vtrd.in/elite.php";

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        WriteToolsTestData.insert(jdbc);
    }

    @Test
    void addsSoftwareOfTheFieldsGiven() {
        assertThat(calls.state("create_software", elite(), MEMBER)).isNull();

        UUID id = calls.write("create_software", elite(), MEMBER).uuid();

        DocumentContext software = software(id);
        assertThat(software.read("$.software.title", String.class)).isEqualTo("Elite");
        assertThat(software.read("$.software.description", String.class)).isEqualTo("Space trading.");
        assertThat(software.read("$.software.releaseDate", String.class)).isEqualTo("1985");
        assertThat(software.read("$.software.platform.code", String.class)).isEqualTo("vector");
        assertThat(ToolJson.list(software, "$.software.hardware[*].code")).containsExactly("vector06c");
        assertThat(ToolJson.list(software, "$.software.categories[*].id")).containsExactly(GAMES.toString());
        assertThat(ToolJson.list(software, "$.software.credits[*].id")).containsExactly(JD.toString());
        assertThat(ToolJson.list(software, "$.software.credits[0].roles")).containsExactly("code");
        assertThat(software.read("$.software.preservation", String.class)).isEqualTo("missing-in-action");
    }

    @Test
    void showsTheSoftwareAsItIsInTheNamesOfTheArguments() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();

        DocumentContext state = ToolJson.of(calls.state("update_software", "{\"id\": \"%s\"}".formatted(id), MEMBER));

        assertThat(state.read("$.title", String.class)).isEqualTo("Elite");
        assertThat(state.read("$.description", String.class)).isEqualTo("Space trading.");
        assertThat(state.read("$.releaseDate", String.class)).isEqualTo("1985");
        assertThat(state.read("$.platform", String.class)).isEqualTo("vector");
        assertThat(ToolJson.list(state, "$.hardware")).containsExactly("vector06c");
        assertThat(ToolJson.list(state, "$.categoryIds")).containsExactly(GAMES.toString());
        assertThat(state.read("$.credits[0].id", String.class)).isEqualTo(JD.toString());
        assertThat(state.read("$.credits[0].type", String.class)).isEqualTo("author-label");
        assertThat(state.read("$.credits[0].title", String.class)).isNotBlank();
        assertThat(ToolJson.list(state, "$.credits[0].roles")).containsExactly("code");
        assertThat(state.read("$.preservation", String.class)).isEqualTo("missing-in-action");
    }

    @Test
    void changesOnlyTheFieldsGiven() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();

        UUID written = calls.write("update_software", """
                        {"id": "%s", "title": "Elite Plus", "releaseDate": "1986-05-12"}
                        """.formatted(id), MEMBER).uuid();

        assertThat(written).isEqualTo(id);
        DocumentContext software = software(id);
        assertThat(software.read("$.software.title", String.class)).isEqualTo("Elite Plus");
        assertThat(software.read("$.software.releaseDate", String.class)).isEqualTo("1986-05-12");
        assertThat(software.read("$.software.description", String.class)).isEqualTo("Space trading.");
        assertThat(ToolJson.list(software, "$.software.hardware[*].code")).containsExactly("vector06c");
        assertThat(ToolJson.list(software, "$.software.categories[*].id")).containsExactly(GAMES.toString());
        assertThat(ToolJson.list(software, "$.software.credits[*].id")).containsExactly(JD.toString());
        assertThat(software.read("$.software.preservation", String.class)).isEqualTo("missing-in-action");
    }

    @Test
    void clearsATextGivenEmptyAndReplacesAListGiven() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();

        calls.write("update_software", """
                {"id": "%s", "description": "", "releaseDate": "", "categoryIds": [], "credits": []}
                """.formatted(id), MEMBER);

        DocumentContext software = software(id);
        assertThat(software.read("$.software.description", String.class)).isNull();
        assertThat(software.read("$.software.releaseDate", String.class)).isNull();
        assertThat(ToolJson.list(software, "$.software.categories")).isEmpty();
        assertThat(ToolJson.list(software, "$.software.credits")).isEmpty();
    }

    @Test
    void keepsTheInstructionsApartFromTheDescription() {
        UUID id = calls.write("create_software", """
                        {"title": "Elite", "platform": "vector", "description": "Space trading.",
                         "instructions": "Press Space to start."}
                        """, MEMBER).uuid();

        assertThat(software(id).read("$.software.instructions", String.class)).isEqualTo("Press Space to start.");
        assertThat(ToolJson.of(calls.state("update_software", "{\"id\": \"%s\"}".formatted(id), MEMBER))
                        .read("$.instructions", String.class))
                .isEqualTo("Press Space to start.");
        calls.write("update_software", "{\"id\": \"%s\", \"instructions\": \"\"}".formatted(id), MEMBER);
        assertThat(software(id).read("$.software.instructions", String.class)).isNull();
        assertThat(software(id).read("$.software.description", String.class)).isEqualTo("Space trading.");
    }

    @Test
    void recordsThePageTheSoftwareWasTakenFromAsItsSource() {
        UUID id = calls.write("create_software", """
                        {"title": "Elite", "platform": "vector", "sourceUrl": "%s"}
                        """.formatted(PAGE), MEMBER).uuid();

        DocumentContext software = software(id);
        assertThat(software.read("$.software.sources[0].archive", String.class)).isEqualTo("zxart.ee");
        assertThat(software.read("$.software.sources[0].url", String.class)).isEqualTo(PAGE);
    }

    @Test
    void addsAnotherSourcePageOnUpdateAndTheSameOneOnce() {
        UUID id = calls.write("create_software", """
                        {"title": "Elite", "platform": "vector", "sourceUrl": "%s"}
                        """.formatted(PAGE), MEMBER).uuid();

        calls.write("update_software", "{\"id\": \"%s\", \"sourceUrl\": \"%s\"}".formatted(id, PAGE), MEMBER);
        calls.write("update_software", "{\"id\": \"%s\", \"sourceUrl\": \"%s\"}".formatted(id, OTHER_PAGE), MEMBER);

        assertThat(ToolJson.list(software(id), "$.software.sources[*].url"))
                .containsExactlyInAnyOrder(PAGE, OTHER_PAGE);
    }

    @Test
    void refusesThePageOfOtherSoftwareAddingNothing() {
        calls.write("create_software", """
                {"title": "Elite", "platform": "vector", "sourceUrl": "%s"}
                """.formatted(PAGE), MEMBER);

        assertThatThrownBy(() -> calls.write("create_software", """
                        {"title": "Elite 2", "platform": "vector", "sourceUrl": "%s"}
                        """.formatted(PAGE), MEMBER))
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo("software.page-taken"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE title = 'Elite 2'", Integer.class))
                .isZero();
    }

    @Test
    void deletesSoftwareShowingTheReleasesThatGoWithIt() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();
        UUID release = calls.write("create_release", """
                        {"softwareId": "%s", "type": "original"}
                        """.formatted(id), MEMBER).uuid();

        DocumentContext state = ToolJson.of(calls.state("delete_software", "{\"id\": \"%s\"}".formatted(id), MEMBER));
        calls.write("delete_software", "{\"id\": \"%s\"}".formatted(id), MEMBER);

        assertThat(state.read("$.software.title", String.class)).isEqualTo("Elite");
        assertThat(ToolJson.list(state, "$.releases[*].id")).containsExactly(release.toString());
        assertThatThrownBy(() -> software(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void creditsSomebodyOrChangesTheirRoles() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();

        calls.write("set_software_credit", """
                {"softwareId": "%s", "id": "%s", "type": "group", "roles": ["publisher"]}
                """.formatted(id, CREW), MEMBER);
        UUID written = calls.write("set_software_credit", """
                        {"softwareId": "%s", "id": "%s", "type": "author-label", "roles": ["code", "music"]}
                        """.formatted(id, JD), MEMBER)
                .uuid();

        assertThat(written).isEqualTo(id);
        DocumentContext software = software(id);
        assertThat(ToolJson.list(software, "$.software.credits[*].id"))
                .containsExactlyInAnyOrder(JD.toString(), CREW.toString());
        assertThat(ToolJson.list(software, "$.software.credits[?(@.id == '%s')].roles[*]".formatted(JD)))
                .containsExactly("code", "music");
        assertThat(ToolJson.list(software, "$.software.credits[?(@.id == '%s')].roles[*]".formatted(CREW)))
                .containsExactly("publisher");
    }

    @Test
    void takesACreditAwayAndRefusesOneThatIsNotThere() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();
        String remove = "{\"softwareId\": \"%s\", \"id\": \"%s\"}";

        DocumentContext state = ToolJson.of(calls.state("remove_software_credit", remove.formatted(id, JD), MEMBER));
        calls.write("remove_software_credit", remove.formatted(id, JD), MEMBER);

        assertThat(ToolJson.list(state, "$.credits[*].id")).containsExactly(JD.toString());
        assertThat(ToolJson.list(software(id), "$.software.credits")).isEmpty();
        assertThatThrownBy(() -> calls.state("remove_software_credit", remove.formatted(id, JANE), MEMBER))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refusesAnIdOfNothing() {
        assertThatThrownBy(() -> calls.state("update_software", "{\"id\": \"%s\"}".formatted(JANE), MEMBER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> calls.state("delete_software", "{\"id\": \"%s\"}".formatted(JANE), MEMBER))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void leavesWhoMayChangeWhatToTheArchive() {
        UUID id = calls.write("create_software", elite(), MEMBER).uuid();

        assertThatThrownBy(() -> calls.write("update_software", """
                        {"id": "%s", "title": "Elite Plus"}
                        """.formatted(id), OTHER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("delete_software", "{\"id\": \"%s\"}".formatted(id), OTHER))
                .isInstanceOf(BusinessException.class);
    }

    private DocumentContext software(UUID id) {
        return ToolJson.of(calls.read("get_software", "{\"id\": \"%s\"}".formatted(id), MEMBER));
    }

    private static String elite() {
        return """
                {"title": "Elite", "description": "Space trading.", "releaseDate": "1985", "platform": "vector",
                 "hardware": ["vector06c"], "categoryIds": ["%s"],
                 "credits": [{"id": "%s", "type": "author-label", "roles": ["code"]}],
                 "preservation": "missing-in-action"}
                """.formatted(GAMES, JD);
    }
}

package com.archive.backend.assistant.web.software;

import static com.archive.backend.assistant.web.tool.WriteToolsTestData.CRW;
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

/** The changes of releases a plan makes: as the editor, through the use cases of the release form. */
@IntegrationTest
@Transactional
@Rollback
class ReleaseWriteToolsIT {

    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);
    private static final ToolContext OTHER = new ToolContext(TestActors.OTHER, Language.EN);

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID elite;

    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        WriteToolsTestData.insert(jdbc);
        elite = calls.write("create_software", """
                        {"title": "Elite", "platform": "vector", "hardware": ["vector06c", "tape"]}
                        """, MEMBER).id();
    }

    @Test
    void addsAReleaseOfItsSoftware() {
        assertThat(calls.state("create_release", ay(), MEMBER)).isNull();

        UUID id = calls.write("create_release", ay(), MEMBER).id();

        DocumentContext release = release(id);
        assertThat(release.read("$.release.title", String.class)).isEqualTo("Elite AY");
        assertThat(release.read("$.release.version", String.class)).isEqualTo("1.1");
        assertThat(release.read("$.release.type", String.class)).isEqualTo("adaptation");
        assertThat(release.read("$.release.releaseDate", String.class)).isEqualTo("1987");
        assertThat(release.read("$.release.description", String.class)).isEqualTo("With music.");
        assertThat(ToolJson.list(release, "$.release.hardware[*].code")).contains("ay8910");
        assertThat(ToolJson.list(release, "$.release.credits[*].id")).containsExactly(JD.toString());
    }

    @Test
    void showsTheReleaseAsItIsInTheNamesOfTheArguments() {
        UUID id = calls.write("create_release", ay(), MEMBER).id();

        DocumentContext state = ToolJson.of(calls.state("update_release", ids(id), MEMBER));

        assertThat(state.read("$.title", String.class)).isEqualTo("Elite AY");
        assertThat(state.read("$.version", String.class)).isEqualTo("1.1");
        assertThat(state.read("$.type", String.class)).isEqualTo("adaptation");
        assertThat(state.read("$.releaseDate", String.class)).isEqualTo("1987");
        assertThat(state.read("$.description", String.class)).isEqualTo("With music.");
        assertThat(ToolJson.list(state, "$.hardware")).containsExactly("ay8910");
        assertThat(state.read("$.credits[0].id", String.class)).isEqualTo(JD.toString());
        assertThat(ToolJson.list(state, "$.credits[0].roles")).containsExactly("adaptation");
    }

    @Test
    void changesOnlyTheFieldsGiven() {
        UUID id = calls.write("create_release", ay(), MEMBER).id();

        UUID written =
                calls.write("update_release", """
                        {"softwareId": "%s", "releaseId": "%s", "version": "1.2", "description": ""}
                        """.formatted(elite, id), MEMBER).id();

        assertThat(written).isEqualTo(id);
        DocumentContext release = release(id);
        assertThat(release.read("$.release.version", String.class)).isEqualTo("1.2");
        assertThat(release.read("$.release.description", String.class)).isNull();
        assertThat(release.read("$.release.title", String.class)).isEqualTo("Elite AY");
        assertThat(release.read("$.release.type", String.class)).isEqualTo("adaptation");
        assertThat(ToolJson.list(release, "$.release.credits[*].id")).containsExactly(JD.toString());
    }

    @Test
    void deletesARelease() {
        UUID id = calls.write("create_release", ay(), MEMBER).id();

        DocumentContext state = ToolJson.of(calls.state("delete_release", ids(id), MEMBER));
        calls.write("delete_release", ids(id), MEMBER);

        assertThat(state.read("$.release.title", String.class)).isEqualTo("Elite AY");
        assertThatThrownBy(() -> release(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void creditsSomebodyOnAReleaseOrTakesTheCreditAway() {
        UUID id = calls.write("create_release", ay(), MEMBER).id();

        calls.write("set_release_credit", """
                {"softwareId": "%s", "releaseId": "%s", "id": "%s", "type": "group-label", "roles": ["intro_code"]}
                """.formatted(elite, id, CRW), MEMBER);
        calls.write("remove_release_credit", """
                {"softwareId": "%s", "releaseId": "%s", "id": "%s"}
                """.formatted(elite, id, JD), MEMBER);

        DocumentContext release = release(id);
        assertThat(ToolJson.list(release, "$.release.credits[*].id")).containsExactly(CRW.toString());
        assertThat(ToolJson.list(release, "$.release.credits[0].roles")).containsExactly("intro_code");
        assertThatThrownBy(() -> calls.write("remove_release_credit", """
                        {"softwareId": "%s", "releaseId": "%s", "id": "%s"}
                        """.formatted(elite, id, JANE), MEMBER))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refusesAReleaseOfOtherSoftwareAndLeavesWhoMayChangeItToTheArchive() {
        UUID id = calls.write("create_release", ay(), MEMBER).id();

        assertThatThrownBy(() -> calls.state("update_release", """
                        {"softwareId": "%s", "releaseId": "%s"}
                        """.formatted(JANE, id), MEMBER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> calls.write("delete_release", ids(id), OTHER)).isInstanceOf(BusinessException.class);
    }

    private DocumentContext release(UUID id) {
        return ToolJson.of(calls.read("get_release", ids(id), MEMBER));
    }

    private String ids(UUID release) {
        return "{\"softwareId\": \"%s\", \"releaseId\": \"%s\"}".formatted(elite, release);
    }

    private String ay() {
        return """
                {"softwareId": "%s", "title": "Elite AY", "version": "1.1", "type": "adaptation", "releaseDate": "1987",
                 "description": "With music.", "hardware": ["ay8910"],
                 "credits": [{"id": "%s", "type": "author-label", "roles": ["adaptation"]}]}
                """.formatted(elite, JD);
    }
}

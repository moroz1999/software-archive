package com.archive.backend.assistant.web.author;

import static com.archive.backend.assistant.web.tool.WriteToolsTestData.FINLAND;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.HELSINKI;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.JANE;
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

/** The changes of people a plan makes: as the editor, through the use cases of the person form. */
@IntegrationTest
@Transactional
@Rollback
class AuthorWriteToolsIT {

    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);
    private static final ToolContext OTHER = new ToolContext(TestActors.OTHER, Language.EN);

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
    void addsAPersonWithTheirLabelsAndLocations() {
        assertThat(calls.state("create_author", ivan(), MEMBER)).isNull();

        UUID id = calls.write("create_author", ivan(), MEMBER).uuid();

        DocumentContext author = author(id);
        assertThat(author.read("$.author.realName", String.class)).isEqualTo("Ivan Petrov");
        assertThat(author.read("$.author.notes", String.class)).isEqualTo("Coder.");
        assertThat(ToolJson.list(author, "$.author.labels[*].name")).containsExactlyInAnyOrder("IP", "Ivan");
        assertThat(ToolJson.list(author, "$.author.labels[?(@.display == true)].name"))
                .containsExactly("IP");
        assertThat(ToolJson.list(author, "$.author.locations[*].cityId")).containsExactly(HELSINKI.toString());
    }

    @Test
    void showsThePersonAsTheyAreInTheNamesOfTheArguments() {
        UUID id = calls.write("create_author", ivan(), MEMBER).uuid();

        DocumentContext state = ToolJson.of(calls.state("update_author", "{\"id\": \"%s\"}".formatted(id), MEMBER));

        assertThat(state.read("$.realName", String.class)).isEqualTo("Ivan Petrov");
        assertThat(state.read("$.notes", String.class)).isEqualTo("Coder.");
        assertThat(ToolJson.list(state, "$.labels[*].name")).containsExactlyInAnyOrder("IP", "Ivan");
        assertThat(ToolJson.list(state, "$.labels[*].id")).hasSize(2);
        assertThat(ToolJson.list(state, "$.labels[?(@.display == true)].name")).containsExactly("IP");
        assertThat(state.read("$.locations[0].countryId", String.class)).isEqualTo(FINLAND.toString());
        assertThat(state.read("$.locations[0].cityId", String.class)).isEqualTo(HELSINKI.toString());
        assertThat(state.read("$.locations[0].primary", Boolean.class)).isTrue();
    }

    @Test
    void changesOnlyTheFieldsGiven() {
        UUID id = calls.write("create_author", ivan(), MEMBER).uuid();

        UUID written = calls.write("update_author", """
                        {"id": "%s", "notes": "Coder and musician.", "realName": ""}
                        """.formatted(id), MEMBER).uuid();

        assertThat(written).isEqualTo(id);
        DocumentContext author = author(id);
        assertThat(author.read("$.author.notes", String.class)).isEqualTo("Coder and musician.");
        assertThat(author.read("$.author.realName", String.class)).isNull();
        assertThat(ToolJson.list(author, "$.author.labels[*].name")).containsExactlyInAnyOrder("IP", "Ivan");
        assertThat(ToolJson.list(author, "$.author.locations[*].cityId")).containsExactly(HELSINKI.toString());
    }

    @Test
    void renamesALabelOfThePersonAndAddsAnother() {
        UUID id = calls.write("create_author", ivan(), MEMBER).uuid();
        DocumentContext state = ToolJson.of(calls.state("update_author", "{\"id\": \"%s\"}".formatted(id), MEMBER));
        String ip = ToolJson.list(state, "$.labels[?(@.name == 'IP')].id")
                .getFirst()
                .toString();

        calls.write("update_author", """
                {"id": "%s", "labels": [{"id": "%s", "name": "I.P.", "display": true}, {"name": "Petrovich"}]}
                """.formatted(id, ip), MEMBER);

        DocumentContext author = author(id);
        assertThat(ToolJson.list(author, "$.author.labels[*].name")).containsExactlyInAnyOrder("I.P.", "Petrovich");
        assertThat(ToolJson.list(author, "$.author.labels[?(@.name == 'I.P.')].id"))
                .containsExactly(ip);
    }

    @Test
    void deletesAPersonLeavingTheirLabelsToNobody() {
        UUID id = calls.write("create_author", ivan(), MEMBER).uuid();
        String ip = ToolJson.list(author(id), "$.author.labels[?(@.name == 'IP')].id")
                .getFirst()
                .toString();

        DocumentContext state = ToolJson.of(calls.state("delete_author", "{\"id\": \"%s\"}".formatted(id), MEMBER));
        calls.write("delete_author", "{\"id\": \"%s\"}".formatted(id), MEMBER);

        assertThat(state.read("$.author.realName", String.class)).isEqualTo("Ivan Petrov");
        assertThatThrownBy(() -> author(id)).isInstanceOf(NotFoundException.class);
        DocumentContext label = ToolJson.of(calls.read("get_label", "{\"id\": \"%s\"}".formatted(ip), MEMBER));
        assertThat(label.read("$.label.owner", Object.class)).isNull();
    }

    @Test
    void refusesAnIdOfNothingAndLeavesWhoMayChangeWhomToTheArchive() {
        UUID id = calls.write("create_author", ivan(), MEMBER).uuid();

        assertThatThrownBy(() -> calls.state("update_author", "{\"id\": \"%s\"}".formatted(FINLAND), MEMBER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> calls.write("update_author", """
                        {"id": "%s", "notes": "Mine."}
                        """.formatted(id), OTHER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("delete_author", "{\"id\": \"%s\"}".formatted(JANE), MEMBER))
                .isInstanceOf(BusinessException.class);
    }

    private DocumentContext author(UUID id) {
        return ToolJson.of(calls.read("get_author", "{\"id\": \"%s\"}".formatted(id), MEMBER));
    }

    private static String ivan() {
        return """
                {"realName": "Ivan Petrov", "notes": "Coder.",
                 "labels": [{"name": "IP", "display": true}, {"name": "Ivan"}],
                 "locations": [{"countryId": "%s", "cityId": "%s", "primary": true}]}
                """.formatted(FINLAND, HELSINKI);
    }
}

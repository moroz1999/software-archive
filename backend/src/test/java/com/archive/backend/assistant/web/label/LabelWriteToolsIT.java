package com.archive.backend.assistant.web.label;

import static com.archive.backend.assistant.web.tool.WriteToolsTestData.CREW;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.FINLAND;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.GHOST;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.JANE;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.JANE_ALT;
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

/** The changes of labels a plan makes: as the editor, through the use cases of the label page. */
@IntegrationTest
@Transactional
@Rollback
class LabelWriteToolsIT {

    private static final ToolContext ADMIN = new ToolContext(TestActors.ADMIN, Language.EN);
    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);

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
    void addsALabelOfNobodyWithItsLocations() {
        String zork = """
                {"name": "Zork", "locations": [{"countryId": "%s", "primary": true}]}
                """.formatted(FINLAND);
        assertThat(calls.state("create_label", zork, MEMBER)).isNull();

        UUID id = calls.write("create_label", zork, MEMBER).uuid();

        DocumentContext label = label(id);
        assertThat(label.read("$.label.name", String.class)).isEqualTo("Zork");
        assertThat(label.read("$.label.owner", Object.class)).isNull();
        assertThat(ToolJson.list(label, "$.label.locations[*].countryId")).containsExactly(FINLAND.toString());
    }

    @Test
    void renamesALabelKeepingItsOwner() {
        DocumentContext state = ToolJson.of(calls.state("update_label", "{\"id\": \"%s\"}".formatted(JANE_ALT), ADMIN));

        calls.write("update_label", "{\"id\": \"%s\", \"name\": \"Jane Other\"}".formatted(JANE_ALT), ADMIN);

        assertThat(state.read("$.name", String.class)).isEqualTo("Jane Alt");
        assertThat(ToolJson.list(state, "$.locations")).isEmpty();
        assertThat(state.read("$.owner.id", String.class)).isEqualTo(JANE.toString());
        DocumentContext label = label(JANE_ALT);
        assertThat(label.read("$.label.name", String.class)).isEqualTo("Jane Other");
        assertThat(label.read("$.label.owner.id", String.class)).isEqualTo(JANE.toString());
    }

    @Test
    void attachesALabelOfNobodyToAPersonOrAGroup() {
        calls.write("attach_label", """
                {"id": "%s", "ownerType": "author", "ownerId": "%s", "display": false}
                """.formatted(GHOST, JANE), ADMIN);
        assertThat(label(GHOST).read("$.label.owner.id", String.class)).isEqualTo(JANE.toString());

        calls.write("detach_label", "{\"id\": \"%s\"}".formatted(GHOST), ADMIN);
        assertThat(label(GHOST).read("$.label.owner", Object.class)).isNull();

        calls.write("attach_label", """
                {"id": "%s", "ownerType": "group", "ownerId": "%s"}
                """.formatted(GHOST, CREW), ADMIN);
        assertThat(label(GHOST).read("$.label.owner.id", String.class)).isEqualTo(CREW.toString());
    }

    @Test
    void takesALabelAwayFromItsOwner() {
        DocumentContext state = ToolJson.of(calls.state("detach_label", "{\"id\": \"%s\"}".formatted(JANE_ALT), ADMIN));

        calls.write("detach_label", "{\"id\": \"%s\"}".formatted(JANE_ALT), ADMIN);

        assertThat(state.read("$.label.owner.id", String.class)).isEqualTo(JANE.toString());
        assertThat(label(JANE_ALT).read("$.label.owner", Object.class)).isNull();
    }

    @Test
    void givesALabelToAGroup() {
        calls.write("give_label_to_group", "{\"id\": \"%s\", \"groupId\": \"%s\"}".formatted(GHOST, CREW), ADMIN);

        assertThat(label(GHOST).read("$.label.owner.id", String.class)).isEqualTo(CREW.toString());
    }

    @Test
    void mergesALabelIntoAPersonAGroupOrAnotherLabel() {
        UUID first = label("First");
        UUID second = label("Second");

        DocumentContext state =
                ToolJson.of(calls.state("merge_label_into_author", merge(GHOST, "authorId", JANE), ADMIN));
        UUID person = calls.write("merge_label_into_author", merge(GHOST, "authorId", JANE), ADMIN)
                .uuid();
        UUID group = calls.write("merge_label_into_group", merge(first, "groupId", CREW), ADMIN)
                .uuid();
        UUID label = calls.write("merge_label_into_label", merge(second, "labelId", JANE_ALT), ADMIN)
                .uuid();

        assertThat(state.read("$.label.name", String.class)).isEqualTo("Ghost");
        assertThat(person).isEqualTo(JANE);
        assertThat(group).isEqualTo(CREW);
        assertThat(label).isEqualTo(JANE_ALT);
        assertThatThrownBy(() -> label(GHOST)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> label(first)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> label(second)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void makesAPersonOrAGroupOfALabel() {
        UUID first = label("First");

        UUID person = calls.write("make_author_of_label", "{\"id\": \"%s\"}".formatted(GHOST), ADMIN)
                .uuid();
        UUID group =
                calls.write("make_group_of_label", """
                        {"id": "%s", "types": ["demogroup"]}
                        """.formatted(first), ADMIN).uuid();

        DocumentContext author = ToolJson.of(calls.read("get_author", "{\"id\": \"%s\"}".formatted(person), ADMIN));
        assertThat(author.read("$.author.name", String.class)).isEqualTo("Ghost");
        DocumentContext made = ToolJson.of(calls.read("get_group", "{\"id\": \"%s\"}".formatted(group), ADMIN));
        assertThat(made.read("$.group.name", String.class)).isEqualTo("First");
        assertThat(ToolJson.list(made, "$.group.types")).containsExactly("demogroup");
    }

    @Test
    void deletesALabel() {
        calls.write("delete_label", "{\"id\": \"%s\"}".formatted(GHOST), ADMIN);

        assertThatThrownBy(() -> label(GHOST)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void refusesAnIdOfNothingAndLeavesWhoMayChangeWhatToTheArchive() {
        jdbc.update("UPDATE person SET real_name = NULL WHERE id = ?", JANE);
        jdbc.update("UPDATE label SET person_id = NULL WHERE id = ?", JANE_ALT);

        assertThatThrownBy(() -> calls.state("update_label", "{\"id\": \"%s\"}".formatted(JANE), ADMIN))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> calls.write("attach_label", """
                        {"id": "%s", "ownerType": "author", "ownerId": "%s"}
                        """.formatted(GHOST, JANE), MEMBER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("delete_label", "{\"id\": \"%s\"}".formatted(JD), ADMIN))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        error -> assertThat(error.code()).isEqualTo("author.nameless"));
    }

    private UUID label(String name) {
        return calls.write("create_label", "{\"name\": \"%s\"}".formatted(name), ADMIN)
                .uuid();
    }

    private DocumentContext label(UUID id) {
        return ToolJson.of(calls.read("get_label", "{\"id\": \"%s\"}".formatted(id), ADMIN));
    }

    private static String merge(UUID label, String field, UUID into) {
        return "{\"id\": \"%s\", \"%s\": \"%s\"}".formatted(label, field, into);
    }
}

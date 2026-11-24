package com.archive.backend.assistant.web.group;

import static com.archive.backend.assistant.web.tool.WriteToolsTestData.CREW;
import static com.archive.backend.assistant.web.tool.WriteToolsTestData.FINLAND;
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

/** The changes of groups a plan makes: as the editor, through the use cases of the group form. */
@IntegrationTest
@Transactional
@Rollback
class GroupWriteToolsIT {

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
    void addsAGroupWithItsLabelsLocationsAndMembers() {
        assertThat(calls.state("create_group", demoCrew(), MEMBER)).isNull();

        UUID id = calls.write("create_group", demoCrew(), MEMBER).uuid();

        DocumentContext group = group(id);
        assertThat(group.read("$.group.name", String.class)).isEqualTo("Demo Crew");
        assertThat(group.read("$.group.notes", String.class)).isEqualTo("From Finland.");
        assertThat(ToolJson.list(group, "$.group.types")).containsExactly("demogroup");
        assertThat(ToolJson.list(group, "$.group.labels[*].name")).containsExactly("DC");
        assertThat(ToolJson.list(group, "$.group.locations[*].countryId")).containsExactly(FINLAND.toString());
        assertThat(ToolJson.list(group, "$.group.members[*].id")).containsExactly(JD.toString());
        assertThat(ToolJson.list(group, "$.group.members[0].roles")).containsExactly("coder");
    }

    @Test
    void showsTheGroupAsItIsInTheNamesOfTheArguments() {
        UUID id = calls.write("create_group", demoCrew(), MEMBER).uuid();

        DocumentContext state = ToolJson.of(calls.state("update_group", "{\"id\": \"%s\"}".formatted(id), MEMBER));

        assertThat(state.read("$.name", String.class)).isEqualTo("Demo Crew");
        assertThat(state.read("$.notes", String.class)).isEqualTo("From Finland.");
        assertThat(ToolJson.list(state, "$.types")).containsExactly("demogroup");
        assertThat(ToolJson.list(state, "$.labels[*].name")).containsExactly("DC");
        assertThat(ToolJson.list(state, "$.labels[*].id")).hasSize(1);
        assertThat(state.read("$.locations[0].countryId", String.class)).isEqualTo(FINLAND.toString());
        assertThat(state.read("$.members[0].id", String.class)).isEqualTo(JD.toString());
        assertThat(state.read("$.members[0].type", String.class)).isEqualTo("author-label");
        assertThat(ToolJson.list(state, "$.members[0].roles")).containsExactly("coder");
    }

    @Test
    void changesOnlyTheFieldsGiven() {
        UUID id = calls.write("create_group", demoCrew(), MEMBER).uuid();

        UUID written = calls.write("update_group", """
                        {"id": "%s", "name": "Demo Crew Inc", "types": ["demogroup", "game studio"]}
                        """.formatted(id), MEMBER).uuid();

        assertThat(written).isEqualTo(id);
        DocumentContext group = group(id);
        assertThat(group.read("$.group.name", String.class)).isEqualTo("Demo Crew Inc");
        assertThat(ToolJson.list(group, "$.group.types")).containsExactlyInAnyOrder("demogroup", "game studio");
        assertThat(group.read("$.group.notes", String.class)).isEqualTo("From Finland.");
        assertThat(ToolJson.list(group, "$.group.labels[*].name")).containsExactly("DC");
        assertThat(ToolJson.list(group, "$.group.members[*].id")).containsExactly(JD.toString());
    }

    @Test
    void makesSomebodyAMemberOrChangesTheirRolesOrTakesThemOut() {
        UUID id = calls.write("create_group", demoCrew(), MEMBER).uuid();

        calls.write("set_group_member", """
                {"groupId": "%s", "id": "%s", "type": "author", "roles": ["musician"]}
                """.formatted(id, JANE), MEMBER);
        calls.write("set_group_member", """
                {"groupId": "%s", "id": "%s", "type": "author-label", "roles": ["coder", "tester"]}
                """.formatted(id, JD), MEMBER);
        DocumentContext state = ToolJson.of(calls.state("remove_group_member", """
                {"groupId": "%s", "id": "%s"}
                """.formatted(id, JANE), MEMBER));
        calls.write("remove_group_member", """
                {"groupId": "%s", "id": "%s"}
                """.formatted(id, JANE), MEMBER);

        assertThat(ToolJson.list(state, "$.members[*].id")).containsExactlyInAnyOrder(JD.toString(), JANE.toString());
        DocumentContext group = group(id);
        assertThat(ToolJson.list(group, "$.group.members[*].id")).containsExactly(JD.toString());
        assertThat(ToolJson.list(group, "$.group.members[0].roles")).containsExactly("coder", "tester");
        assertThatThrownBy(() -> calls.write("remove_group_member", """
                        {"groupId": "%s", "id": "%s"}
                        """.formatted(id, JANE_ALT), MEMBER))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deletesAGroup() {
        UUID id = calls.write("create_group", demoCrew(), MEMBER).uuid();

        DocumentContext state = ToolJson.of(calls.state("delete_group", "{\"id\": \"%s\"}".formatted(id), MEMBER));
        calls.write("delete_group", "{\"id\": \"%s\"}".formatted(id), MEMBER);

        assertThat(state.read("$.group.name", String.class)).isEqualTo("Demo Crew");
        assertThatThrownBy(() -> group(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void refusesAnIdOfNothingAndLeavesWhoMayChangeWhatToTheArchive() {
        UUID id = calls.write("create_group", demoCrew(), MEMBER).uuid();

        assertThatThrownBy(() -> calls.state("update_group", "{\"id\": \"%s\"}".formatted(JANE), MEMBER))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> calls.write("update_group", """
                        {"id": "%s", "notes": "Mine."}
                        """.formatted(id), OTHER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("delete_group", "{\"id\": \"%s\"}".formatted(CREW), MEMBER))
                .isInstanceOf(BusinessException.class);
    }

    private DocumentContext group(UUID id) {
        return ToolJson.of(calls.read("get_group", "{\"id\": \"%s\"}".formatted(id), MEMBER));
    }

    private static String demoCrew() {
        return """
                {"name": "Demo Crew", "notes": "From Finland.", "types": ["demogroup"], "labels": [{"name": "DC"}],
                 "locations": [{"countryId": "%s", "primary": true}],
                 "members": [{"id": "%s", "type": "author-label", "roles": ["coder"]}]}
                """.formatted(FINLAND, JD);
    }
}

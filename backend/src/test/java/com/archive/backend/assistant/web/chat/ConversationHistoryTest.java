package com.archive.backend.assistant.web.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.archive.backend.assistant.app.ListSkillsHandler;
import com.archive.backend.assistant.app.LoadSkillHandler;
import com.archive.backend.assistant.app.LoadSkillQuery;
import com.archive.backend.assistant.domain.skill.SkillName;
import com.archive.backend.assistant.infra.ClasspathSkillReadRepository;
import com.archive.backend.assistant.read.Skill;
import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelToolCall;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversationHistoryTest {

    private static final String SOFTWARE = "00000000-0000-0000-0001-000000000001";

    private final ListSkillsHandler skills = mock(ListSkillsHandler.class);
    private final ConversationHistory history = new ConversationHistory(skills);
    private long seq;

    @BeforeEach
    void skills() {
        when(skills.handle(any()))
                .thenReturn(List.of(
                        new Skill("import-software", "Adding software from a page of another site.", "..."),
                        new Skill("find-person", "Finding which person a name means.", "...")));
    }

    @Test
    void startsWithTheRulesOfTheArchiveAndTheSkillsToLoad() {
        List<ModelMessage> told = history.build(List.of());

        assertThat(told).hasSize(1);
        assertThat(told.get(0).role()).isEqualTo(ModelRole.SYSTEM);
        assertThat(told.get(0).text())
                .contains("language the editor writes in")
                .contains("propose_plan")
                .contains("{\"$ref\": N}")
                .contains("[[type:id]]")
                .contains("has no place for")
                .contains("category")
                .contains("with its country")
                .contains("load_skill")
                .contains("- import-software: Adding software from a page of another site.")
                .contains("- find-person: Finding which person a name means.");
    }

    @Test
    void tellsTheAssistantToAskBeforeProceedingWhenAMaterialIssueIsUnresolved() {
        assertThat(history.build(List.of()).get(0).text())
                .contains("Never silently skip a requested item or a material issue")
                .contains(
                        "stop, explain what you found, ask the editor a specific question, and wait for their answer");
    }

    @Test
    void tellsTheAssistantToImportDistinctNamesakesAndReportSimilarPrograms() {
        LoadSkillHandler loadSkill = new LoadSkillHandler(new ClasspathSkillReadRepository());

        assertThat(loadSkill
                        .handle(new LoadSkillQuery(new SkillName("import-software")))
                        .instructions())
                .contains("Different programs can share a title")
                .contains("same-titled programs by different authors, propose a separate entry")
                .contains("Tell the editor which similar programs you found")
                .contains("If a plausible match remains unresolved, ask the editor how to proceed");
    }

    @Test
    void givesCompilationInstructionsOnlyInTheSoftwareImportSkill() {
        ClasspathSkillReadRepository skills = new ClasspathSkillReadRepository();
        ConversationHistory history = new ConversationHistory(new ListSkillsHandler(skills));
        LoadSkillHandler loadSkill = new LoadSkillHandler(skills);

        assertThat(history.build(List.of()).get(0).text()).doesNotContainIgnoringCase("compilation");
        assertThat(loadSkill
                        .handle(new LoadSkillQuery(new SkillName("sort-out-duplicates")))
                        .instructions())
                .doesNotContainIgnoringCase("compilation");
        assertThat(loadSkill
                        .handle(new LoadSkillQuery(new SkillName("import-software")))
                        .instructions())
                .containsIgnoringCase("compilation")
                .contains("add_compilation_items");
    }

    @Test
    void aMessageOfTheEditorTellsThePageItWasWrittenFrom() {
        List<ModelMessage> told =
                history.build(List.of(message("user", "text", "Добавь её", """
                {"page": {"path": "/software/%s", "title": "Elite", "entityType": "software", "entityId": "%s"}}
                """.formatted(SOFTWARE, SOFTWARE))));

        ModelMessage message = told.get(1);
        assertThat(message.role()).isEqualTo(ModelRole.USER);
        assertThat(message.text())
                .startsWith("Добавь её")
                .contains("/software/" + SOFTWARE)
                .contains("Elite")
                .contains("[[software:" + SOFTWARE + "]]");
    }

    @Test
    void aMessageFromNoPageIsAsWritten() {
        List<ModelMessage> told = history.build(List.of(message("user", "text", "Привет", "{}")));

        assertThat(told.get(1)).isEqualTo(ModelMessage.of(ModelRole.USER, "Привет"));
    }

    @Test
    void theAssistantsProseIsItsOwn() {
        List<ModelMessage> told = history.build(List.of(message("assistant", "text", "Нашёл.", null)));

        assertThat(told.get(1)).isEqualTo(ModelMessage.of(ModelRole.ASSISTANT, "Нашёл."));
    }

    @Test
    void aCallOfAToolIsTheCallAndItsResult() {
        List<ModelMessage> told = history.build(List.of(message("assistant", "toolCall", "", """
                {"callId": "c1", "tool": "search_software", "arguments": "{\\"query\\": \\"Elite\\"}",
                 "result": "{\\"total\\": 0}"}
                """)));

        assertThat(told.subList(1, 3))
                .containsExactly(
                        ModelMessage.calls(
                                List.of(new ModelToolCall("c1", "search_software", "{\"query\": \"Elite\"}"))),
                        ModelMessage.result("c1", "{\"total\": 0}"));
    }

    @Test
    void aProposedPlanIsTheCallOfProposePlanWaitingForTheEditor() {
        List<ModelMessage> told = history.build(List.of(message("assistant", "plan", "", """
                {"callId": "c2", "planId": "00000000-0000-0000-0009-000000000001",
                 "arguments": "{\\"title\\": \\"Add Elite\\"}"}
                """)));

        assertThat(told.get(1))
                .isEqualTo(ModelMessage.calls(
                        List.of(new ModelToolCall("c2", "propose_plan", "{\"title\": \"Add Elite\"}"))));
        assertThat(told.get(2).role()).isEqualTo(ModelRole.TOOL);
        assertThat(told.get(2).toolCallId()).isEqualTo("c2");
        assertThat(told.get(2).text()).contains("waits for the editor");
    }

    @Test
    void whatCameOfAPlanAndAStopAreNotesAndErrorsAreLeftOut() {
        List<ModelMessage> told = history.build(List.of(
                message("system", "planProgress", "Change 1 of 2 (create_software) done: id-1.", "{}"),
                message("system", "error", "The model failed.", null),
                message("system", "stopped", "", null)));

        assertThat(told.subList(1, told.size()))
                .extracting(ModelMessage::role, ModelMessage::text)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                ModelRole.SYSTEM, "Change 1 of 2 (create_software) done: id-1."),
                        org.assertj.core.groups.Tuple.tuple(
                                ModelRole.SYSTEM, "The editor stopped the assistant here."));
    }

    private ChatMessage message(String role, String kind, String text, @Nullable String payload) {
        return new ChatMessage(++seq, role, kind, text, payload, Instant.parse("2026-11-25T10:00:00Z"));
    }
}

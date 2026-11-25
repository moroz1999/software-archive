package com.archive.backend.assistant.web.chat;

import static com.archive.backend.assistant.ScriptedLanguageModel.call;
import static com.archive.backend.assistant.ScriptedLanguageModel.lastResult;
import static com.archive.backend.assistant.ScriptedLanguageModel.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.ScriptedLanguageModel;
import com.archive.backend.assistant.app.chat.FailUnfinishedRunsHandler;
import com.archive.backend.assistant.domain.chat.AssistantBusyException;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.ConversationNotFoundException;
import com.archive.backend.assistant.domain.chat.ModelFailedException;
import com.archive.backend.assistant.domain.chat.PageContext;
import com.archive.backend.assistant.domain.chat.PlanNotProposedException;
import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunRepository;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.assistant.domain.chat.RunStop;
import com.archive.backend.assistant.domain.chat.RunUsage;
import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.chat.ChatPlan;
import com.archive.backend.assistant.read.chat.ChatPlanAction;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import com.archive.backend.assistant.read.chat.ConversationState;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelRequest;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelTool;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.remote.TestImages;
import com.archive.backend.remote.TestSite;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UserGroup;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * The chat with the assistant as an editor has it: messages answered by a scripted model with the real tools, plans
 * proposed, approved and made, runs stopped and limited.
 */
@IntegrationTest
@Transactional
@Rollback
class AssistantChatIT {

    private static final Actor EDITOR =
            new Actor(TestActors.MEMBER_ID, Set.of(UserGroup.MEMBER, UserGroup.AI_ASSISTANT));
    private static final Actor OTHER_EDITOR =
            new Actor(TestActors.OTHER_ID, Set.of(UserGroup.MEMBER, UserGroup.AI_ASSISTANT));
    private static final ToolContext AS_EDITOR = new ToolContext(EDITOR, Language.RU);

    @Autowired
    private ChatService chat;

    @Autowired
    private ScriptedLanguageModel model;

    @Autowired
    private ToolCalls calls;

    @Autowired
    private RunRepository runs;

    @Autowired
    private FailUnfinishedRunsHandler restart;

    @Autowired
    private JdbcTemplate jdbc;

    private final TestSite site = TestSite.start();
    private UUID elite;

    @BeforeEach
    void insert() {
        model.reset();
        TestActors.store(jdbc);
        elite = calls.write("create_software", "{\"title\": \"Elite\", \"platform\": \"vector\"}", AS_EDITOR)
                .uuid();
    }

    @AfterEach
    void stop() {
        site.close();
        model.reset();
    }

    // Who talks to it

    @Test
    void onlyTheMembersOfTheAssistantsGroupTalkToIt() {
        assertThatThrownBy(() -> chat.conversations(TestActors.MEMBER)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> chat.create(TestActors.ADMIN)).isInstanceOf(ChangeForbiddenException.class);
        assertThatThrownBy(() -> chat.create(Actor.anonymous())).isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void aConversationOfAnotherEditorDoesNotExistForThem() {
        UUID conversation = chat.create(EDITOR);

        assertThatThrownBy(() -> chat.state(OTHER_EDITOR, conversation, 0))
                .isInstanceOf(ConversationNotFoundException.class);
        assertThatThrownBy(() -> send(OTHER_EDITOR, conversation, "Привет"))
                .isInstanceOf(ConversationNotFoundException.class);
        assertThatThrownBy(() -> chat.delete(OTHER_EDITOR, conversation))
                .isInstanceOf(ConversationNotFoundException.class);
        assertThat(chat.conversations(OTHER_EDITOR)).isEmpty();
    }

    // Conversations

    @Test
    void listsTheConversationsOfTheEditorTheLatestFirstTitledByTheirFirstMessage() {
        UUID first = chat.create(EDITOR);
        model.replies("Здравствуйте.");
        send(EDITOR, first, "Привет\nкак дела");
        UUID second = chat.create(EDITOR);
        chat.create(OTHER_EDITOR);

        assertThat(chat.conversations(EDITOR))
                .extracting(ConversationListEntry::id, ConversationListEntry::title, ConversationListEntry::running)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(second, null, false),
                        org.assertj.core.groups.Tuple.tuple(first, "Привет как дела", false));
    }

    @Test
    void deletesAConversationWithAllItHolds() {
        UUID conversation = chat.create(EDITOR);
        model.replies("Здравствуйте.");
        send(EDITOR, conversation, "Привет");

        chat.delete(EDITOR, conversation);

        assertThat(chat.conversations(EDITOR)).isEmpty();
        assertThatThrownBy(() -> chat.state(EDITOR, conversation, 0)).isInstanceOf(ConversationNotFoundException.class);
    }

    // Answering

    @Test
    void answersAMessageAndKnowsThePageItWasWrittenFrom() {
        UUID conversation = chat.create(EDITOR);
        model.replies("Это [[software:%s]].".formatted(elite));

        chat.send(
                EDITOR,
                Language.RU,
                conversation,
                "Что это за игра?",
                new PageContext("/software/" + elite, "Elite", "software", elite.toString()));

        ConversationState state = chat.state(EDITOR, conversation, 0);
        assertThat(state.messages())
                .extracting(ChatMessage::role, ChatMessage::kind, ChatMessage::text)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("user", "text", "Что это за игра?"),
                        org.assertj.core.groups.Tuple.tuple(
                                "assistant", "text", "Это [[software:%s]].".formatted(elite)));
        assertThat(state.messages().get(0).payload()).contains("/software/" + elite);
        assertThat(state.activeRun()).isNull();
        ModelRequest request = model.lastRequest();
        assertThat(request.messages().get(0).role()).isEqualTo(ModelRole.SYSTEM);
        assertThat(last(request).text()).contains("Что это за игра?").contains("[[software:" + elite + "]]");
    }

    @Test
    void namesTheEntriesItMentionsAsLinks() {
        UUID conversation = chat.create(EDITOR);
        UUID nothing = UUID.randomUUID();
        model.replies("Нашёл [[software:%s]], но не [[software:%s]].".formatted(elite, nothing));

        send(EDITOR, conversation, "Найди Elite");

        Map<String, com.archive.backend.assistant.read.chat.EntityRef> refs =
                chat.state(EDITOR, conversation, 0).refs();
        assertThat(refs).containsOnlyKeys(elite.toString());
        assertThat(refs.get(elite.toString()))
                .isEqualTo(new com.archive.backend.assistant.read.chat.EntityRef(
                        "software", elite.toString(), "Elite", "/software/" + elite));
    }

    @Test
    void namesTheCategoriesAndTheHardwareAPlanChoosesAsLinks() {
        UUID games = UUID.randomUUID();
        jdbc.update("INSERT INTO category (id, parent_id, position) VALUES (?, NULL, 0)", games);
        jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'ru', 'Игры')", games);
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", """
                {"title": "Elite 2", "actions": [{"tool": "create_software", "arguments": {
                  "title": "Elite 2", "platform": "vector", "hardware": ["vector06c"], "categoryIds": ["%s"]}}]}
                """.formatted(games));

        send(EDITOR, conversation, "Добавь Elite 2");

        Map<String, com.archive.backend.assistant.read.chat.EntityRef> refs =
                chat.state(EDITOR, conversation, 0).refs();
        assertThat(refs.get(games.toString()))
                .isEqualTo(new com.archive.backend.assistant.read.chat.EntityRef(
                        "category", games.toString(), "Игры", "/software/category/" + games));
        assertThat(refs).containsKey("vector06c");
    }

    @Test
    void givesOnlyTheMessagesAfterTheOneAskedFor() {
        UUID conversation = chat.create(EDITOR);
        model.replies("Здравствуйте.");
        send(EDITOR, conversation, "Привет");
        long first = chat.state(EDITOR, conversation, 0).messages().get(0).seq();

        assertThat(chat.state(EDITOR, conversation, first).messages())
                .extracting(ChatMessage::text)
                .containsExactly("Здравствуйте.");
    }

    @Test
    void offersTheToolsThatReadAndThoseOfPlansButNoneThatWrites() {
        UUID conversation = chat.create(EDITOR);
        model.replies("Здравствуйте.");

        send(EDITOR, conversation, "Привет");

        assertThat(model.lastRequest().tools())
                .extracting(ModelTool::name)
                .contains("search_software", "get_software", "load_skill", "describe_write_tool", "propose_plan")
                .doesNotContain("create_software", "delete_software", "add_screenshots");
    }

    @Test
    void callsAToolThatReadsAndTellsTheModelItsResult() {
        UUID conversation = chat.create(EDITOR);
        model.calls("get_software", "{\"id\": \"%s\"}".formatted(elite)).replies("Это Elite.");

        send(EDITOR, conversation, "Что это?");

        assertThat(lastResult(model.lastRequest())).contains("Elite");
        ChatMessage call = chat.state(EDITOR, conversation, 0).messages().get(1);
        assertThat(call.kind()).isEqualTo("toolCall");
        assertThat(call.payload()).contains("get_software").contains("Elite");
        assertThat(texts(conversation)).last().isEqualTo("Это Elite.");
    }

    @Test
    void aCallThatFailsIsToldToTheModelAndItGoesOn() {
        UUID conversation = chat.create(EDITOR);
        model.calls("no_such_tool", "{}")
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:").contains("no_such_tool");
                    return call("create_software", "{\"title\": \"Elite 2\", \"platform\": \"vector\"}");
                })
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:").contains("plan");
                    return call("get_software", "{\"id\": \"%s\"}".formatted(UUID.randomUUID()));
                })
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:");
                    return text("Не вышло.");
                });

        send(EDITOR, conversation, "Добавь Elite 2");

        assertThat(model.requests()).hasSize(4);
        assertThat(texts(conversation)).last().isEqualTo("Не вышло.");
        assertThat(software("Elite 2")).isZero();
    }

    @Test
    void describesAToolThatWritesBeforeItGoesIntoAPlan() {
        UUID conversation = chat.create(EDITOR);
        model.calls("describe_write_tool", "{\"name\": \"create_software\"}")
                .then(request -> {
                    assertThat(lastResult(request))
                            .contains("create_software")
                            .contains("\"kind\"")
                            .contains("\"schema\"")
                            .contains("platform");
                    return call("describe_write_tool", "{\"name\": \"get_software\"}");
                })
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:");
                    return text("Понял.");
                });

        send(EDITOR, conversation, "Как добавить игру?");

        assertThat(model.requests()).hasSize(3);
    }

    @Test
    void showsThePictureOfViewImageToTheModelOnlyInTheRunThatLookedAtIt() {
        byte[] png = TestImages.png(8, 6);
        site.serve("/shot.png", 200, Map.of("Content-Type", "image/png"), png);
        UUID conversation = chat.create(EDITOR);
        model.calls("view_image", "{\"url\": \"%s\"}".formatted(site.url("/shot.png")))
                .then(request -> {
                    ModelMessage shown = last(request);
                    assertThat(shown.role()).isEqualTo(ModelRole.USER);
                    assertThat(shown.images()).hasSize(1);
                    assertThat(shown.images().get(0).mediaType()).isEqualTo("image/png");
                    return text("Это экран игры.");
                })
                .replies("Да.");

        send(EDITOR, conversation, "Что на картинке?");
        send(EDITOR, conversation, "Точно?");

        assertThat(model.lastRequest().messages())
                .allSatisfy(message -> assertThat(message.images()).isEmpty());
        assertThat(chat.state(EDITOR, conversation, 0).messages().get(1).payload())
                .doesNotContain(java.util.Base64.getEncoder().encodeToString(png));
    }

    // Plans

    @Test
    void proposesAPlanWithEachChangeBesideWhatItChangesAndWaits() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", """
                {"title": "Elite и его релиз",
                 "actions": [
                   {"tool": "update_software", "arguments": {"id": "%s", "title": "Elite Plus"}},
                   {"tool": "create_software", "arguments": {"title": "Elite 2", "platform": "vector"}},
                   {"tool": "create_release", "arguments": {"softwareId": {"$ref": 2}, "version": "1.0"}}
                 ]}
                """.formatted(elite));

        send(EDITOR, conversation, "Переименуй Elite и добавь Elite 2 с релизом");

        assertThat(model.requests()).hasSize(1);
        ConversationState state = chat.state(EDITOR, conversation, 0);
        assertThat(state.messages()).extracting(ChatMessage::kind).containsExactly("text", "plan");
        assertThat(state.activeRun()).isNull();
        ChatPlan plan = state.plans().get(0);
        assertThat(plan.title()).isEqualTo("Elite и его релиз");
        assertThat(plan.status()).isEqualTo("proposed");
        List<ChatPlanAction> actions = plan.actions();
        assertThat(actions)
                .extracting(ChatPlanAction::tool, ChatPlanAction::kind, ChatPlanAction::entityType)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("update_software", "update", "software"),
                        org.assertj.core.groups.Tuple.tuple("create_software", "create", "software"),
                        org.assertj.core.groups.Tuple.tuple("create_release", "create", "release"));
        assertThat(actions.get(0).state()).contains("Elite");
        assertThat(actions.get(1).state()).isNull();
        assertThat(actions.get(2).state()).isNull();
        assertThat(actions.get(2).dependsOn()).containsExactly(2);
        assertThat(state.refs()).containsKey(elite.toString());
        assertThat(software("Elite 2")).isZero();
    }

    @Test
    void aPlanThatDoesNotHoldGoesBackToTheModelWithWhy() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", """
                        {"title": "Читать", "actions": [{"tool": "get_software", "arguments": {"id": "%s"}}]}
                        """.formatted(elite))
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:");
                    return call("propose_plan", """
                            {"title": "Удалить", "actions": [{"tool": "delete_software", "arguments": {"id": "%s"}}]}
                            """.formatted(UUID.randomUUID()));
                })
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:").contains("1");
                    return call("propose_plan", """
                            {"title": "Релиз", "actions": [
                              {"tool": "create_release", "arguments": {"softwareId": {"$ref": 2}}},
                              {"tool": "create_software", "arguments": {"title": "Elite 2", "platform": "vector"}}]}
                            """);
                })
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:").contains("earlier");
                    return call("propose_plan", """
                            {"title": "Переименовать", "actions": [
                              {"tool": "update_software", "arguments": {"id": "%s", "colour": "red"}}]}
                            """.formatted(elite));
                })
                .then(request -> {
                    assertThat(lastResult(request)).startsWith("Error:").contains("colour");
                    return text("Не получается составить план.");
                });

        send(EDITOR, conversation, "Сделай что-нибудь");

        assertThat(model.requests()).hasSize(5);
        assertThat(chat.state(EDITOR, conversation, 0).plans()).isEmpty();
    }

    @Test
    void anApprovedPlanIsMadeAsTheEditorAndTheAssistantSumsItUp() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", """
                        {"title": "Elite 2 с релизом", "actions": [
                          {"tool": "create_software", "arguments": {"title": "Elite 2", "platform": "vector"}},
                          {"tool": "create_release", "arguments": {"softwareId": {"$ref": 1}, "version": "1.0"}}]}
                        """).then(request -> {
            assertThat(request.messages())
                    .anySatisfy(message -> assertThat(message.text()).contains("done"));
            return text("Готово: [[software:%s]].".formatted(elite));
        });
        send(EDITOR, conversation, "Добавь Elite 2 с релизом");
        UUID plan = chat.state(EDITOR, conversation, 0).plans().get(0).id();

        chat.approve(EDITOR, Language.RU, plan);

        ConversationState state = chat.state(EDITOR, conversation, 0);
        ChatPlan made = state.plans().get(0);
        assertThat(made.status()).isEqualTo("done");
        assertThat(made.actions()).extracting(ChatPlanAction::status).containsOnly("done");
        String created = made.actions().get(0).result();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_release WHERE software_id = ?::uuid AND version = '1.0'",
                        Integer.class,
                        created))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT added_by FROM software WHERE id = ?::uuid", UUID.class, created))
                .isEqualTo(TestActors.MEMBER_ID.value());
        assertThat(state.messages()).extracting(ChatMessage::kind).contains("planProgress");
        assertThat(state.messages().get(state.messages().size() - 1).text())
                .isEqualTo("Готово: [[software:%s]].".formatted(elite));
        assertThat(state.refs()).containsKey(created);
        assertThat(state.activeRun()).isNull();
    }

    @Test
    void theFirstChangeThatFailsStopsThePlanAndTheAssistantIsToldWhy() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", """
                        {"title": "Удалить и переименовать", "actions": [
                          {"tool": "delete_software", "arguments": {"id": "%s"}},
                          {"tool": "update_software", "arguments": {"id": "%s", "title": "Elite Plus"}},
                          {"tool": "create_software", "arguments": {"title": "Elite 2", "platform": "vector"}}]}
                        """.formatted(elite, elite)).then(request -> {
            assertThat(request.messages())
                    .anySatisfy(message -> assertThat(message.text()).contains("failed"));
            return text("Второй шаг не прошёл: программы уже нет.");
        });
        send(EDITOR, conversation, "Удали Elite и переименуй его");
        UUID plan = chat.state(EDITOR, conversation, 0).plans().get(0).id();

        chat.approve(EDITOR, Language.RU, plan);

        ChatPlan made = chat.state(EDITOR, conversation, 0).plans().get(0);
        assertThat(made.status()).isEqualTo("failed");
        assertThat(made.actions()).extracting(ChatPlanAction::status).containsExactly("done", "failed", "skipped");
        assertThat(made.actions().get(1).error()).isNotBlank();
        assertThat(software("Elite 2")).isZero();
        assertThat(texts(conversation)).last().isEqualTo("Второй шаг не прошёл: программы уже нет.");
    }

    @Test
    void writingAgainDropsThePlanWaitingForTheEditor() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", deleteElite()).replies("Хорошо, не удаляю.");
        send(EDITOR, conversation, "Удали Elite");
        UUID plan = chat.state(EDITOR, conversation, 0).plans().get(0).id();

        send(EDITOR, conversation, "Нет, не надо");

        assertThat(chat.state(EDITOR, conversation, 0).plans().get(0).status()).isEqualTo("superseded");
        assertThatThrownBy(() -> chat.approve(EDITOR, Language.RU, plan)).isInstanceOf(PlanNotProposedException.class);
        assertThat(software("Elite")).isEqualTo(1);
    }

    @Test
    void theEditorCancelsAPlanWithoutAnythingMade() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", deleteElite());
        send(EDITOR, conversation, "Удали Elite");
        UUID plan = chat.state(EDITOR, conversation, 0).plans().get(0).id();

        chat.cancel(EDITOR, plan);

        ConversationState state = chat.state(EDITOR, conversation, 0);
        assertThat(state.plans().get(0).status()).isEqualTo("cancelled");
        assertThat(state.messages()).extracting(ChatMessage::kind).last().isEqualTo("planProgress");
        assertThat(model.requests()).hasSize(1);
        assertThat(software("Elite")).isEqualTo(1);
        assertThatThrownBy(() -> chat.approve(EDITOR, Language.RU, plan)).isInstanceOf(PlanNotProposedException.class);
    }

    @Test
    void aPlanOfAnotherEditorDoesNotExistForThem() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", deleteElite());
        send(EDITOR, conversation, "Удали Elite");
        UUID plan = chat.state(EDITOR, conversation, 0).plans().get(0).id();

        assertThatThrownBy(() -> chat.approve(OTHER_EDITOR, Language.RU, plan)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> chat.cancel(OTHER_EDITOR, plan)).isInstanceOf(NotFoundException.class);
    }

    // Runs

    @Test
    void stopsWhenTheEditorAsksItBeforeItsNextStep() {
        UUID conversation = chat.create(EDITOR);
        model.then(request -> {
            chat.stop(EDITOR, conversation);
            return call("get_software", "{\"id\": \"%s\"}".formatted(elite));
        });

        send(EDITOR, conversation, "Изучи всё");

        assertThat(model.requests()).hasSize(1);
        assertThat(chat.state(EDITOR, conversation, 0).messages())
                .extracting(ChatMessage::kind)
                .last()
                .isEqualTo("stopped");
    }

    @Test
    void oneTaskAtATimeInAConversation() {
        UUID conversation = chat.create(EDITOR);
        List<RuntimeException> refused = new ArrayList<>();
        model.then(request -> {
            for (Runnable attempt : List.<Runnable>of(
                    () -> send(EDITOR, conversation, "И ещё"), () -> chat.delete(EDITOR, conversation))) {
                try {
                    attempt.run();
                } catch (AssistantBusyException e) {
                    refused.add(e);
                }
            }
            return text("Здравствуйте.");
        });

        send(EDITOR, conversation, "Привет");

        assertThat(refused).hasSize(2);
        assertThat(texts(conversation)).containsExactly("Привет", "Здравствуйте.");
    }

    @Test
    void aRunLeftOverAQuarterOfAnHourIsTakenForDead() {
        UUID conversation = chat.create(EDITOR);
        runs.save(new Run(
                new RunId(UUID.randomUUID()),
                new ConversationId(conversation),
                com.archive.backend.assistant.domain.chat.RunType.CHAT,
                null,
                Instant.now().minus(Duration.ofMinutes(16)),
                RunStatus.RUNNING,
                RunStop.NOT_ASKED,
                RunUsage.NONE,
                null,
                null));
        model.replies("Здравствуйте.");

        send(EDITOR, conversation, "Привет");

        assertThat(texts(conversation)).last().isEqualTo("Здравствуйте.");
        assertThat(runs.findActive(new ConversationId(conversation))).isEmpty();
    }

    @Test
    void endsAfter50StepsAsFailed() {
        UUID conversation = chat.create(EDITOR);
        model.always(request -> call("get_software", "{\"id\": \"%s\"}".formatted(elite)));

        send(EDITOR, conversation, "Изучай без конца");

        assertThat(model.requests()).hasSize(50);
        assertThat(chat.state(EDITOR, conversation, 0).messages())
                .extracting(ChatMessage::kind)
                .last()
                .isEqualTo("error");
    }

    @Test
    void aFailureOfTheModelFailsTheRunAndTheNextMessageIsAnsweredAgain() {
        UUID conversation = chat.create(EDITOR);
        model.then(request -> {
                    throw new ModelFailedException("The model could not be asked: overloaded");
                })
                .replies("Здравствуйте.");

        send(EDITOR, conversation, "Привет");
        ChatMessage error = chat.state(EDITOR, conversation, 0).messages().get(1);
        send(EDITOR, conversation, "Ещё раз");

        assertThat(error.kind()).isEqualTo("error");
        assertThat(error.text()).contains("overloaded");
        assertThat(texts(conversation)).last().isEqualTo("Здравствуйте.");
    }

    @Test
    void aRestartFailsWhatWasRunningAndDoesNotMakeAChangeAgain() {
        UUID conversation = chat.create(EDITOR);
        model.calls("propose_plan", deleteElite());
        send(EDITOR, conversation, "Удали Elite");
        UUID plan = chat.state(EDITOR, conversation, 0).plans().get(0).id();
        jdbc.update("UPDATE assistant_plan SET status = 'EXECUTING' WHERE id = ?", plan);
        jdbc.update(
                "UPDATE assistant_plan SET actions = jsonb_set(actions, '{0,status}', '\"RUNNING\"') WHERE id = ?",
                plan);
        runs.save(new Run(
                new RunId(UUID.randomUUID()),
                new ConversationId(conversation),
                com.archive.backend.assistant.domain.chat.RunType.EXECUTE,
                new com.archive.backend.assistant.domain.chat.PlanId(plan),
                Instant.now(),
                RunStatus.RUNNING,
                RunStop.NOT_ASKED,
                RunUsage.NONE,
                null,
                null));

        restart.on();

        ConversationState state = chat.state(EDITOR, conversation, 0);
        assertThat(state.activeRun()).isNull();
        assertThat(state.plans().get(0).status()).isEqualTo("failed");
        assertThat(state.plans().get(0).actions().get(0).status()).isEqualTo("failed");
        assertThat(state.messages()).extracting(ChatMessage::kind).last().isEqualTo("error");
        assertThat(software("Elite")).isEqualTo(1);
    }

    private void send(Actor actor, UUID conversation, String text) {
        chat.send(actor, Language.RU, conversation, text, PageContext.NONE);
    }

    private List<String> texts(UUID conversation) {
        return chat.state(EDITOR, conversation, 0).messages().stream()
                .filter(message -> message.kind().equals("text"))
                .map(ChatMessage::text)
                .toList();
    }

    private String deleteElite() {
        return """
                {"title": "Удалить Elite", "actions": [{"tool": "delete_software", "arguments": {"id": "%s"}}]}
                """.formatted(elite);
    }

    private int software(String title) {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM software WHERE title = ?", Integer.class, title);
        return count == null ? 0 : count;
    }

    private static ModelMessage last(ModelRequest request) {
        return request.messages().get(request.messages().size() - 1);
    }
}

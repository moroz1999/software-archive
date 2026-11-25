package com.archive.backend.assistant.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.assistant.domain.chat.AssistantBusyException;
import com.archive.backend.assistant.domain.chat.ConversationNotFoundException;
import com.archive.backend.assistant.domain.chat.PageContext;
import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.chat.ChatPlan;
import com.archive.backend.assistant.read.chat.ChatPlanAction;
import com.archive.backend.assistant.read.chat.ChatRun;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import com.archive.backend.assistant.read.chat.ConversationState;
import com.archive.backend.assistant.read.chat.EntityRef;
import com.archive.backend.assistant.web.chat.ChatService;
import com.archive.backend.assistant.web.mapper.AssistantWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssistantController.class)
@Import({SecurityConfig.class, WebConfig.class, AssistantWebMapperImpl.class, ApiExceptionHandler.class})
class AssistantControllerTest {

    private static final UUID CONVERSATION = UUID.fromString("00000000-0000-0000-00c0-000000000001");
    private static final UUID PLAN = UUID.fromString("00000000-0000-0000-00c0-000000000002");
    private static final UUID RUN = UUID.fromString("00000000-0000-0000-00c0-000000000003");
    private static final UUID SOFTWARE = UUID.fromString("00000000-0000-0000-0001-000000000001");
    private static final Instant AT = Instant.parse("2026-11-25T10:00:00Z");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ChatService chat;

    @Test
    void listsTheConversationsOfTheEditor() throws Exception {
        when(chat.conversations(Actor.anonymous()))
                .thenReturn(List.of(new ConversationListEntry(CONVERSATION, "Добавь Elite", AT, true)));

        mvc.perform(get("/api/assistant/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CONVERSATION.toString()))
                .andExpect(jsonPath("$[0].title").value("Добавь Elite"))
                .andExpect(jsonPath("$[0].lastMessageAt").value("2026-11-25T10:00:00Z"))
                .andExpect(jsonPath("$[0].running").value(true));
    }

    @Test
    void startsAConversation() throws Exception {
        when(chat.create(Actor.anonymous())).thenReturn(CONVERSATION);

        mvc.perform(post("/api/assistant/conversations"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CONVERSATION.toString()));
    }

    @Test
    void givesAConversationAfterASeqWithWhatItCarriesAsJson() throws Exception {
        when(chat.state(Actor.anonymous(), CONVERSATION, 4))
                .thenReturn(new ConversationState(
                        CONVERSATION,
                        "Добавь Elite",
                        List.of(new ChatMessage(
                                5, "user", "text", "Добавь Elite", "{\"page\": {\"path\": \"/software\"}}", AT)),
                        List.of(new ChatPlan(
                                PLAN,
                                "Elite",
                                "proposed",
                                AT,
                                List.of(new ChatPlanAction(
                                        1,
                                        "update_software",
                                        "{\"id\": \"%s\", \"title\": \"Elite Plus\"}".formatted(SOFTWARE),
                                        List.of(),
                                        "update",
                                        "software",
                                        "{\"title\": \"Elite\"}",
                                        "pending",
                                        null,
                                        null)))),
                        new ChatRun(RUN, CONVERSATION, "chat", null, "running", false, AT, 3, null),
                        Map.of(
                                SOFTWARE.toString(),
                                new EntityRef("software", SOFTWARE.toString(), "Elite", "/software/" + SOFTWARE))));

        mvc.perform(get("/api/assistant/conversations/{id}", CONVERSATION).param("afterSeq", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Добавь Elite"))
                .andExpect(jsonPath("$.messages[0].seq").value(5))
                .andExpect(jsonPath("$.messages[0].payload.page.path").value("/software"))
                .andExpect(jsonPath("$.plans[0].actions[0].arguments.title").value("Elite Plus"))
                .andExpect(jsonPath("$.plans[0].actions[0].state.title").value("Elite"))
                .andExpect(jsonPath("$.activeRun.status").value("running"))
                .andExpect(jsonPath("$.activeRun.steps").value(3))
                .andExpect(jsonPath("$.refs['%s'].path".formatted(SOFTWARE)).value("/software/" + SOFTWARE));
    }

    @Test
    void sendsAMessageWithThePageItWasWrittenFrom() throws Exception {
        mvc.perform(post("/api/assistant/conversations/{id}/messages", CONVERSATION)
                        .header("Accept-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "Добавь её", "page": {"path": "/software/%s", "title": "Elite",
                                 "entityType": "software", "entityId": "%s"}}
                                """.formatted(SOFTWARE, SOFTWARE)))
                .andExpect(status().isAccepted());

        verify(chat)
                .send(
                        Actor.anonymous(),
                        Language.RU,
                        CONVERSATION,
                        "Добавь её",
                        new PageContext("/software/" + SOFTWARE, "Elite", "software", SOFTWARE.toString()));
    }

    @Test
    void aMessageFromNoPageHasNone() throws Exception {
        mvc.perform(post("/api/assistant/conversations/{id}/messages", CONVERSATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Привет\"}"))
                .andExpect(status().isAccepted());

        verify(chat).send(Actor.anonymous(), Language.EN, CONVERSATION, "Привет", PageContext.NONE);
    }

    @Test
    void stopsDeletesApprovesAndCancels() throws Exception {
        mvc.perform(post("/api/assistant/conversations/{id}/stop", CONVERSATION))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/assistant/conversations/{id}", CONVERSATION)).andExpect(status().isNoContent());
        mvc.perform(post("/api/assistant/plans/{id}/approve", PLAN)).andExpect(status().isAccepted());
        mvc.perform(post("/api/assistant/plans/{id}/cancel", PLAN)).andExpect(status().isNoContent());

        verify(chat).stop(Actor.anonymous(), CONVERSATION);
        verify(chat).delete(Actor.anonymous(), CONVERSATION);
        verify(chat).approve(Actor.anonymous(), Language.EN, PLAN);
        verify(chat).cancel(Actor.anonymous(), PLAN);
    }

    @Test
    void answersTheRefusalsOfTheChat() throws Exception {
        when(chat.conversations(any())).thenThrow(new ChangeForbiddenException());
        when(chat.state(any(), any(), anyLong())).thenThrow(new ConversationNotFoundException());
        doThrow(new AssistantBusyException()).when(chat).send(any(), any(), any(), any(), any());

        mvc.perform(get("/api/assistant/conversations")).andExpect(status().isForbidden());
        mvc.perform(get("/api/assistant/conversations/{id}", CONVERSATION))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("assistant.conversation-not-found"));
        mvc.perform(post("/api/assistant/conversations/{id}/messages", CONVERSATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Привет\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("assistant.busy"));
    }
}

package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Conversation;
import com.archive.backend.assistant.domain.chat.ConversationMessage;
import com.archive.backend.assistant.domain.chat.ConversationMessageRepository;
import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.MessageBody;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.PageContext;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stores what the editor wrote from a page, drops the plan waiting for them, and queues a run answering it; refused while the assistant works on the conversation, unless that run is dead. */
@Service
@RequiredArgsConstructor
public class SendMessageHandler {

    private final ConversationRepository conversations;
    private final ConversationMessageRepository messages;
    private final RunRepository runs;
    private final PlanRepository plans;
    private final ObjectMapper json;
    private final Clock clock;

    @Transactional
    public RunId handle(SendMessageCommand c) {
        ChatAccess.requireUser(c.actor());
        Conversation conversation = ChatAccess.owned(conversations, c.conversationId(), c.actor());
        Instant now = clock.instant();
        ChatAccess.requireFree(runs, c.conversationId(), now);
        plans.findProposed(c.conversationId()).ifPresent(plan -> {
            plan.supersede(now);
            plans.save(plan);
        });
        conversation.told(c.text(), now);
        conversations.save(conversation);
        messages.save(new ConversationMessage(
                c.conversationId(),
                MessageRole.USER,
                MessageKind.TEXT,
                new MessageBody(c.text().value(), page(c.page())),
                now));
        RunId run = new RunId(UUID.randomUUID());
        runs.save(Run.chat(run, c.conversationId(), now));
        return run;
    }

    /** The page as the payload of the message: none when it was written from no page. */
    private @Nullable String page(PageContext page) {
        if (page.path() == null && page.entityId() == null) {
            return null;
        }
        ObjectNode payload = json.createObjectNode();
        payload.putObject("page")
                .put("path", page.path())
                .put("title", page.title())
                .put("entityType", page.entityType())
                .put("entityId", page.entityId());
        return payload.toString();
    }
}

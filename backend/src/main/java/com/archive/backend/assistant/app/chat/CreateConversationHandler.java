package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Conversation;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.ConversationRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Starts an empty conversation of the editor. */
@Service
@RequiredArgsConstructor
public class CreateConversationHandler {

    private final ConversationRepository conversations;
    private final Clock clock;

    @Transactional
    public ConversationId handle(CreateConversationCommand c) {
        ChatAccess.requireUser(c.actor());
        ConversationId id = new ConversationId(UUID.randomUUID());
        conversations.save(Conversation.start(id, c.actor().signedIn(), clock.instant()));
        return id;
    }
}

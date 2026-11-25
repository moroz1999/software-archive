package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Conversation;
import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.RunRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Deletes a conversation of the editor with all it holds; refused while the assistant works on it. */
@Service
@RequiredArgsConstructor
public class DeleteConversationHandler {

    private final ConversationRepository conversations;
    private final RunRepository runs;
    private final Clock clock;

    @Transactional
    public void handle(DeleteConversationCommand c) {
        ChatAccess.requireUser(c.actor());
        Conversation conversation = ChatAccess.owned(conversations, c.conversationId(), c.actor());
        ChatAccess.requireFree(runs, c.conversationId(), clock.instant());
        conversations.delete(conversation);
    }
}

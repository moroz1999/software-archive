package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationMessage;
import com.archive.backend.assistant.domain.chat.ConversationMessageRepository;
import com.archive.backend.assistant.domain.chat.MessageSeq;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Appends what a run wrote to its conversation. */
@Service
@RequiredArgsConstructor
public class RecordMessageHandler {

    private final ConversationMessageRepository messages;
    private final Clock clock;

    @Transactional
    public MessageSeq handle(RecordMessageCommand c) {
        return messages.save(
                new ConversationMessage(c.conversationId(), c.role(), c.kind(), c.body(), clock.instant()));
    }
}

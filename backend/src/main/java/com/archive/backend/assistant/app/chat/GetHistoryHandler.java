package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.chat.ChatReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Every message of a conversation, for its run to tell the model. */
@Service
@RequiredArgsConstructor
public class GetHistoryHandler {

    private final ChatReadRepository chats;

    @Transactional(readOnly = true)
    public List<ChatMessage> handle(GetHistoryQuery c) {
        return chats.messages(c.conversationId().value(), 0);
    }
}

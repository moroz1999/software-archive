package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.read.chat.ChatReadRepository;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The editor's conversations, the latest first. */
@Service
@RequiredArgsConstructor
public class ListConversationsHandler {

    private final ChatReadRepository chats;

    @Transactional(readOnly = true)
    public List<ConversationListEntry> handle(ListConversationsQuery c) {
        ChatAccess.requireUser(c.actor());
        return chats.conversations(c.actor().signedIn().value());
    }
}

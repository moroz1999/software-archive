package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.error.NotFoundException;

/** No such conversation, or one of somebody else. */
public class ConversationNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public ConversationNotFoundException() {
        super("assistant.conversation-not-found", "No such conversation.");
    }
}

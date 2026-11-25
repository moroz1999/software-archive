package com.archive.backend.assistant.domain.chat;

/** The messages of conversations, appended in their order. */
public interface ConversationMessageRepository {

    /** Appends the message with the next seq of its conversation. */
    MessageSeq save(ConversationMessage message);
}

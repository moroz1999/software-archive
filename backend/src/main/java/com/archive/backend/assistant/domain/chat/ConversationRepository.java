package com.archive.backend.assistant.domain.chat;

import java.util.Optional;

public interface ConversationRepository {

    Optional<Conversation> findById(ConversationId id);

    void save(Conversation conversation);

    /** Deletes it with its messages, runs and plans. */
    void delete(Conversation conversation);
}

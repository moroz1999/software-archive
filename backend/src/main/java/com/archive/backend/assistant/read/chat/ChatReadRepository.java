package com.archive.backend.assistant.read.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatReadRepository {

    /** The conversations of the owner, the latest first. */
    List<ConversationListEntry> conversations(UUID owner);

    Optional<UUID> owner(UUID conversationId);

    /** The messages after the seq, in their order. */
    List<ChatMessage> messages(UUID conversationId, long afterSeq);

    /** Every plan of the conversation, in the order proposed. */
    List<ChatPlan> plans(UUID conversationId);

    Optional<ChatPlan> plan(UUID planId);

    Optional<ChatRun> activeRun(UUID conversationId);

    Optional<ChatRun> run(UUID runId);
}

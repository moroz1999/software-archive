package com.archive.backend.assistant.domain.chat;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {

    Optional<Plan> findById(PlanId id);

    /** The plan of the conversation waiting for the editor. */
    Optional<Plan> findProposed(ConversationId conversationId);

    /** Every plan being made. */
    List<Plan> findExecuting();

    void save(Plan plan);
}

package com.archive.backend.assistant.domain.chat;

import java.util.List;
import java.util.Optional;

public interface RunRepository {

    Optional<Run> findById(RunId id);

    /** The queued or running run of the conversation. */
    Optional<Run> findActive(ConversationId conversationId);

    /** Every queued or running run. */
    List<Run> findActive();

    void save(Run run);
}

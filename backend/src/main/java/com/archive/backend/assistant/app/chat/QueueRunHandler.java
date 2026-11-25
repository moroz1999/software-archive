package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Queues a run answering the conversation once a plan was made: it sums up what came of it. */
@Service
@RequiredArgsConstructor
public class QueueRunHandler {

    private final RunRepository runs;
    private final Clock clock;

    @Transactional
    public RunId handle(QueueRunCommand c) {
        RunId id = new RunId(UUID.randomUUID());
        runs.save(Run.chat(id, c.conversationId(), clock.instant()));
        return id;
    }
}

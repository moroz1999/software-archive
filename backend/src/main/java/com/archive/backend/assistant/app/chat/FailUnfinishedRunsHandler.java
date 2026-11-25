package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationMessage;
import com.archive.backend.assistant.domain.chat.ConversationMessageRepository;
import com.archive.backend.assistant.domain.chat.MessageBody;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunError;
import com.archive.backend.assistant.domain.chat.RunRepository;
import com.archive.backend.assistant.domain.chat.RunStatus;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A run still queued or running when the application starts was ended by the restart: it failed, and so did the
 * change of a plan being made then, which is not made again; the conversation tells so.
 */
@Service
@RequiredArgsConstructor
public class FailUnfinishedRunsHandler {

    static final String INTERRUPTED = "The assistant was interrupted by a restart of the archive.";

    private final RunRepository runs;
    private final PlanRepository plans;
    private final ConversationMessageRepository messages;
    private final Clock clock;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void on() {
        Instant now = clock.instant();
        for (Plan plan : plans.findExecuting()) {
            plan.interrupt(now);
            plans.save(plan);
        }
        for (Run run : runs.findActive()) {
            run.end(RunStatus.FAILED, new RunError(INTERRUPTED), now);
            runs.save(run);
            messages.save(new ConversationMessage(
                    run.getConversationId(),
                    MessageRole.SYSTEM,
                    MessageKind.ERROR,
                    new MessageBody(INTERRUPTED, null),
                    now));
        }
    }
}

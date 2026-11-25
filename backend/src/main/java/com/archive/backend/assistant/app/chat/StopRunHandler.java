package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.RunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Asks the run working on the conversation to stop; with none, nothing happens. */
@Service
@RequiredArgsConstructor
public class StopRunHandler {

    private final ConversationRepository conversations;
    private final RunRepository runs;

    @Transactional
    public void handle(StopRunCommand c) {
        ChatAccess.requireUser(c.actor());
        ChatAccess.owned(conversations, c.conversationId(), c.actor());
        runs.findActive(c.conversationId()).ifPresent(run -> {
            run.requestStop();
            runs.save(run);
        });
    }
}

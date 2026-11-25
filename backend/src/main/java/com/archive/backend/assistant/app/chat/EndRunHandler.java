package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ends a run, with why it failed. */
@Service
@RequiredArgsConstructor
public class EndRunHandler {

    private final RunRepository runs;
    private final Clock clock;

    @Transactional
    public void handle(EndRunCommand c) {
        Run run = ChatAccess.run(runs, c.runId());
        run.end(c.status(), c.error(), clock.instant());
        runs.save(run);
    }
}

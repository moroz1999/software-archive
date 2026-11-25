package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Takes a queued run to run it: whoever comes second gets nothing. */
@Service
@RequiredArgsConstructor
public class ClaimRunHandler {

    private final RunRepository runs;

    @Transactional
    public boolean handle(ClaimRunCommand c) {
        Run run = ChatAccess.run(runs, c.runId());
        boolean claimed = run.claim();
        runs.save(run);
        return claimed;
    }
}

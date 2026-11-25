package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Counts a request of a run to the model, with its tokens. */
@Service
@RequiredArgsConstructor
public class RecordRunStepHandler {

    private final RunRepository runs;

    @Transactional
    public void handle(RecordRunStepCommand c) {
        Run run = ChatAccess.run(runs, c.runId());
        run.step(c.step().tokensIn(), c.step().tokensOut());
        runs.save(run);
    }
}

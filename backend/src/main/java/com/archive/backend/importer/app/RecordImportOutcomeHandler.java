package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes what one program came to into the report of its run as the run goes; an added program is written in the
 * transaction that adds it, so the report never names software that is not there.
 */
@Service
@RequiredArgsConstructor
public class RecordImportOutcomeHandler {

    private final ImportRunRepository runs;
    private final Clock clock;

    @Transactional
    public void handle(RecordImportOutcomeCommand c) {
        ImportRun run = runs.findById(c.run()).orElseThrow();
        run.record(c.outcome(), clock.instant());
        runs.save(run);
    }
}

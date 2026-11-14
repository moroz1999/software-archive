package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A run still going on when the application starts was stopped by the restart that ended the last one: its report
 * says so, with what it got through (docs/features/vektor-import.md).
 */
@Service
@RequiredArgsConstructor
public class StopUnfinishedImportRunsHandler {

    private final ImportRunRepository runs;
    private final Clock clock;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void on() {
        for (ImportRun run : runs.findRunning()) {
            run.end(ImportRunState.STOPPED, clock.instant());
            runs.save(run);
        }
    }
}

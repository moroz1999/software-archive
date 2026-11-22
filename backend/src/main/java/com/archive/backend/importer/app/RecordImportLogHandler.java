package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes steps into the log of a run as they are done, in a transaction of their own: the administrator sees a page
 * read or a file downloaded while the program is still being taken in, and a program that fails after it keeps them.
 */
@Service
@RequiredArgsConstructor
public class RecordImportLogHandler {

    private final ImportRunRepository runs;
    private final Clock clock;

    @Transactional
    public void handle(RecordImportLogCommand c) {
        if (c.entries().isEmpty()) {
            return;
        }
        ImportRun run = runs.findById(c.run()).orElseThrow();
        Instant now = clock.instant();
        c.entries().forEach(entry -> run.log(entry, now));
        runs.save(run);
    }
}

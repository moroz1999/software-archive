package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes where a run is into its report before each program, in a transaction of its own: the administrator sees it
 * while the program is still being read.
 */
@Service
@RequiredArgsConstructor
public class RecordImportProgressHandler {

    private final ImportRunRepository runs;

    @Transactional
    public void handle(RecordImportProgressCommand c) {
        ImportRun run = runs.findById(c.run()).orElseThrow();
        run.advance(c.progress());
        runs.save(run);
    }
}

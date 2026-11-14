package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EndImportRunHandler {

    private final ImportRunRepository runs;
    private final Clock clock;

    @Transactional
    public void handle(EndImportRunCommand c) {
        ImportRun run = runs.findById(c.run()).orElseThrow();
        run.end(c.end(), clock.instant());
        runs.save(run);
    }
}

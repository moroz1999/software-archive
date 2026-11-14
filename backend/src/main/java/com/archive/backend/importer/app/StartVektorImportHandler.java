package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.vektor.VektorSite;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Starts the import of vektor-06c.ru in the background and returns at once: a run takes far longer than a request
 * waits. Only administrators start it, and only one runs at a time (docs/features/vektor-import.md).
 */
@Service
public class StartVektorImportHandler {

    private static final Logger log = LoggerFactory.getLogger(StartVektorImportHandler.class);

    private final ImportVektorHandler importer;
    private final ImportRunRepository runs;
    private final EndImportRunHandler endRun;
    private final Clock clock;
    private final Executor executor;
    private final AtomicBoolean running = new AtomicBoolean();

    /** The run goes to the application's pool of background threads, not to the scheduler of the search worker. */
    public StartVektorImportHandler(
            ImportVektorHandler importer,
            ImportRunRepository runs,
            EndImportRunHandler endRun,
            Clock clock,
            @Qualifier("applicationTaskExecutor") Executor executor) {
        this.importer = importer;
        this.runs = runs;
        this.endRun = endRun;
        this.clock = clock;
        this.executor = executor;
    }

    public void handle(StartVektorImportCommand c) {
        c.actor().administrator();
        if (!running.compareAndSet(false, true)) {
            throw new ImportRunningException();
        }
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        try {
            // Saved, and so committed, before the run starts: the run writes its report into it from another thread.
            runs.save(ImportRun.start(id, new ImportScope(VektorSite.ARCHIVE, c.section()), clock.instant()));
            executor.execute(() -> {
                try {
                    importer.handle(new ImportVektorCommand(id, c.section()));
                } catch (RuntimeException e) {
                    log.error("The import of vektor-06c.ru stopped", e);
                    endRun.handle(new EndImportRunCommand(id, ImportRunState.STOPPED));
                } finally {
                    running.set(false);
                }
            });
        } catch (RuntimeException e) {
            running.set(false);
            throw e;
        }
    }
}

package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ImportRunningException;
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
    private final Executor executor;
    private final AtomicBoolean running = new AtomicBoolean();

    /** The run goes to the application's pool of background threads, not to the scheduler of the search worker. */
    public StartVektorImportHandler(
            ImportVektorHandler importer, @Qualifier("applicationTaskExecutor") Executor executor) {
        this.importer = importer;
        this.executor = executor;
    }

    public void handle(StartVektorImportCommand c) {
        c.actor().administrator();
        if (!running.compareAndSet(false, true)) {
            throw new ImportRunningException();
        }
        ImportVektorCommand run = new ImportVektorCommand(c.section());
        try {
            executor.execute(() -> {
                try {
                    importer.handle(run);
                } catch (RuntimeException e) {
                    log.error("The import of vektor-06c.ru stopped", e);
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

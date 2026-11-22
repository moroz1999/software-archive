package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.basis.BasisCategory;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.run.ImportSection;
import com.archive.backend.importer.domain.vektor.VektorSection;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Starts the import of a source archive in the background and returns at once: a run takes far longer than a request
 * waits. Only administrators start it, and only one import runs at a time, whatever its source
 * (docs/features/imports.md).
 */
@Service
public class StartImportHandler {

    private static final Logger log = LoggerFactory.getLogger(StartImportHandler.class);

    private final ImportVektorHandler vektor;
    private final ImportBasisHandler basis;
    private final ImportRunRepository runs;
    private final EndImportRunHandler endRun;
    private final Clock clock;
    private final Executor executor;
    private final AtomicBoolean running = new AtomicBoolean();

    /** The run goes to the application's pool of background threads, not to the scheduler of the search worker. */
    public StartImportHandler(
            ImportVektorHandler vektor,
            ImportBasisHandler basis,
            ImportRunRepository runs,
            EndImportRunHandler endRun,
            Clock clock,
            @Qualifier("applicationTaskExecutor") Executor executor) {
        this.vektor = vektor;
        this.basis = basis;
        this.runs = runs;
        this.endRun = endRun;
        this.clock = clock;
        this.executor = executor;
    }

    public void handle(StartImportCommand c) {
        c.actor().administrator();
        if (!running.compareAndSet(false, true)) {
            throw new ImportRunningException();
        }
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        try {
            // Saved, and so committed, before the run starts: the run writes its report into it from another thread.
            runs.save(ImportRun.start(id, new ImportScope(c.source().archive(), c.section()), clock.instant()));
            executor.execute(() -> {
                try {
                    switch (c.source()) {
                        case VEKTOR -> vektor.handle(new ImportVektorCommand(id, vektorSection(c.section())));
                        case BASIS -> basis.handle(new ImportBasisCommand(id, basisCategory(c.section())));
                    }
                } catch (RuntimeException e) {
                    log.error("The import of {} stopped", c.source().archive(), e);
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

    private static @Nullable VektorSection vektorSection(@Nullable ImportSection section) {
        return (VektorSection) section;
    }

    private static @Nullable BasisCategory basisCategory(@Nullable ImportSection section) {
        return (BasisCategory) section;
    }
}

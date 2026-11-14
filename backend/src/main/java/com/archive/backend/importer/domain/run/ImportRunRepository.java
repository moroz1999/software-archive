package com.archive.backend.importer.domain.run;

import java.util.List;
import java.util.Optional;

/** The runs of imports with their reports. */
public interface ImportRunRepository {

    Optional<ImportRun> findById(ImportRunId id);

    /** The runs not ended yet. */
    List<ImportRun> findRunning();

    void save(ImportRun run);
}

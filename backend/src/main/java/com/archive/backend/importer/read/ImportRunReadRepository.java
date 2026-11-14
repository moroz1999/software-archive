package com.archive.backend.importer.read;

import java.util.Optional;

/** The reports of the runs of imports. */
public interface ImportRunReadRepository {

    /** The report of the run started last, of whatever archive. */
    Optional<ImportRunReport> findLatest();
}

package com.archive.backend.importer.read;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** The reports of the runs of imports. */
public interface ImportRunReadRepository {

    /**
     * The report of the run started last, of whatever archive, with the lines of its log after the one given; all of
     * them without one.
     */
    Optional<ImportRunReport> findLatest(@Nullable Long after);
}

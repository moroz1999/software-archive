package com.archive.backend.importer.read;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The report of a run of an import.
 *
 * @param section the code of the section imported; the whole archive without one
 * @param state {@code RUNNING}, {@code DONE} or {@code STOPPED}
 */
public record ImportRunReport(
        UUID id,
        String archive,
        @Nullable String section,
        String state,
        Instant startedAt,
        @Nullable Instant endedAt,
        int added,
        int known,
        int skipped,
        int failed,
        List<ImportRunAddedEntry> addedPrograms,
        List<ImportRunFailedEntry> failedPrograms) {}

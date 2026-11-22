package com.archive.backend.importer.read;

import java.util.UUID;

/**
 * An author a run took for the first time.
 *
 * @param kind {@code author}, {@code group} or {@code label}
 * @param created whether the run created it; otherwise it found it in the archive
 * @param exists whether it is still in the archive
 */
public record ImportRunAuthorEntry(String kind, UUID id, String name, boolean created, boolean exists)
        implements ImportRunLogEntry {}

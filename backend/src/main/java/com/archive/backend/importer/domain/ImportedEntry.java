package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import java.util.Objects;
import lombok.Getter;

/**
 * An entry of the archive an import added, remembered by its origin so that the next run does not add it again
 * (docs/domain/imports.md).
 */
@Getter
@AggregateRoot
public final class ImportedEntry {

    private final ExternalOrigin origin;
    private final ImportedEntryId entry;

    public ImportedEntry(ExternalOrigin origin, ImportedEntryId entry) {
        this.origin = Objects.requireNonNull(origin, "origin");
        this.entry = Objects.requireNonNull(entry, "entry");
    }
}

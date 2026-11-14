package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * An entry of the archive an import added, remembered by its origin so that the next run does not add it again
 * (docs/domain/imports.md).
 */
@Getter
@AggregateRoot
public final class ImportedEntry {

    private final ExternalOrigin origin;
    private final ImportedEntryId entry;
    private final @Nullable ExternalPage page;

    /**
     * @param page its page in the source archive; a name taken as a label has none
     */
    public ImportedEntry(ExternalOrigin origin, ImportedEntryId entry, @Nullable ExternalPage page) {
        this.origin = Objects.requireNonNull(origin, "origin");
        this.entry = Objects.requireNonNull(entry, "entry");
        this.page = page;
    }

    public ImportedEntry(ExternalOrigin origin, ImportedEntryId entry) {
        this(origin, entry, null);
    }
}

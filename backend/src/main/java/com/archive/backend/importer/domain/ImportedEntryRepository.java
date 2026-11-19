package com.archive.backend.importer.domain;

import java.util.Optional;

/**
 * The entries imports added, by their origin.
 */
public interface ImportedEntryRepository {

    Optional<ImportedEntry> findByOrigin(ExternalOrigin origin);

    void save(ImportedEntry entry);

    /** Whether the entry of this kind came from this source archive. */
    boolean isFrom(String archive, ExternalKind kind, ImportedEntryId entry);

    /** Forgets where the entry of this kind came from, so an import may take it again. */
    void forget(ExternalKind kind, ImportedEntryId entry);

    /** Remembers what came as one entry as another, which replaced it. */
    void transfer(ExternalKind kind, ImportedEntryId entry, ExternalKind toKind, ImportedEntryId to);
}

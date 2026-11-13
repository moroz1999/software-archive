package com.archive.backend.importer.domain;

import java.util.Optional;

/**
 * The entries imports added, by their origin.
 */
public interface ImportedEntryRepository {

    Optional<ImportedEntry> findByOrigin(ExternalOrigin origin);

    void save(ImportedEntry entry);
}

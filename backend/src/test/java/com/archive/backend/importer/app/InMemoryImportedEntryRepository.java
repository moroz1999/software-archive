package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The entries imports added, kept in memory. */
class InMemoryImportedEntryRepository implements ImportedEntryRepository {

    final List<ImportedEntry> entries = new ArrayList<>();

    @Override
    public Optional<ImportedEntry> findByOrigin(ExternalOrigin origin) {
        return entries.stream()
                .filter(entry -> entry.getOrigin().equals(origin))
                .findFirst();
    }

    @Override
    public void save(ImportedEntry entry) {
        entries.removeIf(stored -> stored.getOrigin().equals(entry.getOrigin()));
        entries.add(entry);
    }

    @Override
    public boolean isFrom(String archive, ExternalKind kind, ImportedEntryId entry) {
        return entries.stream()
                .anyMatch(stored -> stored.getOrigin().archive().equals(archive)
                        && stored.getOrigin().kind() == kind
                        && stored.getEntry().equals(entry));
    }

    @Override
    public void forget(ExternalKind kind, ImportedEntryId entry) {
        entries.removeIf(
                stored -> stored.getOrigin().kind() == kind && stored.getEntry().equals(entry));
    }

    @Override
    public void transfer(ExternalKind kind, ImportedEntryId entry, ExternalKind toKind, ImportedEntryId to) {
        throw new UnsupportedOperationException();
    }
}

package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.importer.domain.SoftwarePageTakenException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records the page of another site as a source of the software, as an import records the page of a program it took
 * (docs/domain/imports.md). A page is the source of one software only.
 */
@Service
@RequiredArgsConstructor
public class RecordSoftwarePageHandler {

    private final ImportedEntryRepository repository;

    @Transactional
    public void handle(RecordSoftwarePageCommand c) {
        ExternalOrigin origin = ExternalOrigin.softwarePage(c.page());
        Optional<ImportedEntry> stored = repository.findByOrigin(origin);
        if (stored.isPresent()) {
            if (!stored.get().getEntry().equals(c.software())) {
                throw new SoftwarePageTakenException();
            }
            return;
        }
        repository.save(new ImportedEntry(origin, c.software(), c.page()));
    }
}

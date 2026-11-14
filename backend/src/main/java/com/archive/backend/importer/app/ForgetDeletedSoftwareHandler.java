package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.software.domain.SoftwareDeleted;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Software deleted in the archive is forgotten by the imports with it, in the same transaction, so the next run takes
 * the program again (docs/domain/imports.md).
 */
@Service
@RequiredArgsConstructor
public class ForgetDeletedSoftwareHandler {

    private final ImportedEntryRepository entries;

    @EventListener
    public void on(SoftwareDeleted deleted) {
        entries.forget(
                ExternalKind.SOFTWARE, new ImportedEntryId(deleted.software().value()));
    }
}

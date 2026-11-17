package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.label.domain.LabelDeleted;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A label deleted in the archive is forgotten by the imports with it, in the same transaction, so the next import
 * meeting the name takes it as a new label (docs/domain/imports.md).
 */
@Service
@RequiredArgsConstructor
public class ForgetDeletedLabelHandler {

    private final ImportedEntryRepository entries;

    @EventListener
    public void on(LabelDeleted deleted) {
        entries.forget(ExternalKind.LABEL, new ImportedEntryId(deleted.label().value()));
    }
}

package com.archive.backend.importer.app;

import com.archive.backend.author.domain.AuthorDeleted;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A person deleted in the archive is forgotten by the imports with it, in the same transaction, so the next import
 * meeting them takes them as new (docs/domain/imports.md#origin).
 */
@Service
@RequiredArgsConstructor
public class ForgetDeletedAuthorHandler {

    private final ImportedEntryRepository entries;

    @EventListener
    public void on(AuthorDeleted deleted) {
        entries.forget(ExternalKind.AUTHOR, new ImportedEntryId(deleted.author().value()));
    }
}

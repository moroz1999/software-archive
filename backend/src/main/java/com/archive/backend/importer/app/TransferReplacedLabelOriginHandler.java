package com.archive.backend.importer.app;

import com.archive.backend.author.domain.LabelReplacedByAuthor;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A label replaced by a person is remembered by the imports as that person, in the same transaction, so the next import
 * meeting the name finds the person (docs/domain/imports.md).
 */
@Service
@RequiredArgsConstructor
public class TransferReplacedLabelOriginHandler {

    private final ImportedEntryRepository entries;

    @EventListener
    public void on(LabelReplacedByAuthor replaced) {
        entries.transfer(
                ExternalKind.LABEL,
                new ImportedEntryId(replaced.label().value()),
                ExternalKind.AUTHOR,
                new ImportedEntryId(replaced.author().value()));
    }
}

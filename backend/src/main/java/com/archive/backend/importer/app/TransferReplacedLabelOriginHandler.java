package com.archive.backend.importer.app;

import com.archive.backend.author.domain.LabelReplacedByAuthor;
import com.archive.backend.group.domain.LabelReplacedByGroup;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.label.domain.LabelMergedIntoLabel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A label replaced by a person, a group or another label is remembered by the imports as them, in the same transaction,
 * so the next import meeting the name finds them (docs/domain/imports.md).
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

    @EventListener
    public void on(LabelReplacedByGroup replaced) {
        entries.transfer(
                ExternalKind.LABEL,
                new ImportedEntryId(replaced.label().value()),
                ExternalKind.GROUP,
                new ImportedEntryId(replaced.group().value()));
    }

    @EventListener
    public void on(LabelMergedIntoLabel merged) {
        entries.transfer(
                ExternalKind.LABEL,
                new ImportedEntryId(merged.label().value()),
                ExternalKind.LABEL,
                new ImportedEntryId(merged.into().value()));
    }
}

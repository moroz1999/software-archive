package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportedEntryId;

/** The page of another site the software was taken from. */
public record RecordSoftwarePageCommand(ImportedEntryId software, ExternalPage page) {}

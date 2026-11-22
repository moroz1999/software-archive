package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportLogEntry;
import com.archive.backend.importer.domain.run.ImportRunId;
import java.util.List;

/** Logs steps of a run that are not what a program came to: a page of the list read, the files of a program. */
public record RecordImportLogCommand(ImportRunId run, List<ImportLogEntry> entries) {

    public RecordImportLogCommand {
        entries = List.copyOf(entries);
    }
}

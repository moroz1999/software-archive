package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportRunId;

/** Writes what one program came to into the report of its run. */
public record RecordImportOutcomeCommand(ImportRunId run, ImportOutcome outcome) {}

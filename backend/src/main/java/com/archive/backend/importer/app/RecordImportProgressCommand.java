package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRunId;

/** Tells the report of a run which program of the source it goes to now. */
public record RecordImportProgressCommand(ImportRunId run, ImportProgress progress) {}

package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunState;

/** Ends a run of an import: done, or stopped before its end. */
public record EndImportRunCommand(ImportRunId run, ImportRunState end) {}

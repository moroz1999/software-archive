package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRunId;

/** Imports every card of the index "Базис", writing what it does into the report of the run. */
public record ImportBasisCommand(ImportRunId run) {}

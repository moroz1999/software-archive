package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.basis.BasisCategory;
import com.archive.backend.importer.domain.run.ImportRunId;
import org.jspecify.annotations.Nullable;

/** Imports every card of "Базис", or those in one category, and writes what it does into the report of the run. */
public record ImportBasisCommand(ImportRunId run, @Nullable BasisCategory category) {}

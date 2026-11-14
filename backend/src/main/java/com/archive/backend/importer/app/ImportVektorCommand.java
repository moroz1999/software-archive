package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.vektor.VektorSection;
import org.jspecify.annotations.Nullable;

/** Imports every program of vektor-06c.ru, or of one of its sections, writing what it does into the report of the run. */
public record ImportVektorCommand(ImportRunId run, @Nullable VektorSection section) {}

package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.bkcatalog.BkCatalogSection;
import com.archive.backend.importer.domain.run.ImportRunId;
import org.jspecify.annotations.Nullable;

/** Runs the import of bk-catalog: of every section, or of the one given. */
public record ImportBkCatalogCommand(
        ImportRunId run, @Nullable BkCatalogSection section) {}

package com.archive.backend.importer.web.dto;

import org.jspecify.annotations.Nullable;

/** The run of an import started last; none before the first one. */
public record LastImportRunDto(@Nullable ImportRunDto run) {}

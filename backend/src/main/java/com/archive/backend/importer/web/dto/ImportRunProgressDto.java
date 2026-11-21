package com.archive.backend.importer.web.dto;

import org.jspecify.annotations.Nullable;

/** As {@code ImportRunProgressEntry} of the report. */
public record ImportRunProgressDto(
        int position, @Nullable Integer total, @Nullable String url) {}

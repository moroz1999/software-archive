package com.archive.backend.importer.web.dto;

import org.jspecify.annotations.Nullable;

/** As {@code ImportRunListReadEntry} of the report. */
public record ImportRunListReadDto(
        String url, int number, @Nullable Integer pages, int programs) {}

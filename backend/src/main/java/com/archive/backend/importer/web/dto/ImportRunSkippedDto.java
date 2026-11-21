package com.archive.backend.importer.web.dto;

/** As {@code ImportRunSkippedEntry} of the report. */
public record ImportRunSkippedDto(String externalId, String url, String title, String reason, String detail) {}

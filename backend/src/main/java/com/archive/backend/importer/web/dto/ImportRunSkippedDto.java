package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** As {@code ImportRunSkippedEntry} of the report. */
public record ImportRunSkippedDto(
        String externalId,
        String url,
        String title,
        @Schema(ref = "SkipReason") String reason,
        String detail) {}

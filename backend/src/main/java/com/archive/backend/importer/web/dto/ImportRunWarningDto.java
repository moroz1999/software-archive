package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** As {@code ImportRunWarningEntry} of the report. */
public record ImportRunWarningDto(
        String externalId,
        String url,
        String title,
        @Schema(ref = "ImportWarningKind") String kind,
        String value) {}

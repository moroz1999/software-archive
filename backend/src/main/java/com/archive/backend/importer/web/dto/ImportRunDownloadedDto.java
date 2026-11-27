package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** As {@code ImportRunDownloadedEntry} of the report. */
public record ImportRunDownloadedDto(
        String externalId,
        String title,
        @Schema(ref = "DownloadedFile") String file,
        String name,
        long size) {}

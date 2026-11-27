package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** As {@code ImportRunAuthorEntry} of the report. */
public record ImportRunAuthorDto(
        @Schema(ref = "TakenAuthorKind") String kind, UUID id, String name, boolean created, boolean exists) {}

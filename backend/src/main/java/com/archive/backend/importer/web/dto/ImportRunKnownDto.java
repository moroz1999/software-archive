package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

/** As {@code ImportRunKnownEntry} of the report. */
public record ImportRunKnownDto(
        String externalId,
        UUID softwareId,
        String title,
        boolean exists,

        @ArraySchema(schema = @Schema(ref = "SoftwareSupplement"))
        List<String> supplements) {}

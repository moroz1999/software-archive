package com.archive.backend.software.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Where the work comes from (docs/domain/software.md#lineage); `null` for every part not known. */
public record SoftwareLineageDto(
        @Schema(ref = "LineageKind") @Nullable String kind,
        @Schema(ref = "LineageReliability") @Nullable String reliability,

        @ArraySchema(schema = @Schema(ref = "LineageRework"))
        List<String> rework,

        @Nullable String source) {}

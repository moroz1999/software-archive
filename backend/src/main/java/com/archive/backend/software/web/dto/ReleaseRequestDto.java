package com.archive.backend.software.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A release as sent to add or edit it; {@code hardware} lists the codes of its own hardware, {@code credits}
 * whoever is credited on it as on software.
 */
public record ReleaseRequestDto(
        @Nullable String title,
        @Nullable String version,
        @Schema(ref = "ReleaseType") @Nullable String type,
        @Nullable String releaseDate,
        @Nullable String description,
        @Nullable List<String> hardware,
        @Nullable List<SoftwareRequestDto.CreditDto> credits) {}

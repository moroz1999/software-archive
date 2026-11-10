package com.archive.backend.software.web.dto;

import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A release as sent to add or edit it; {@code hardware} lists the codes of its own hardware.
 */
public record ReleaseRequestDto(
        @Nullable String title,
        @Nullable String version,
        @Nullable String type,
        @Nullable LocalDate releaseDate,
        @Nullable String description,
        @Nullable List<String> hardware) {}

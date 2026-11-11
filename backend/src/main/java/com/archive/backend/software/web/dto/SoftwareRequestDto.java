package com.archive.backend.software.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing software.
 */
public record SoftwareRequestDto(
        @Nullable String title,
        @Nullable String description,
        @Nullable LocalDate releaseDate,
        @Nullable String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> categories) {}

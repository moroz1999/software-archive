package com.archive.backend.software.web.dto;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/** Body of creating and of editing software. */
public record SoftwareRequestDto(
        @Nullable String title,
        @Nullable String description,
        @Nullable LocalDate releaseDate) {}

package com.archive.backend.software.web.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/** Body of creating and of editing software. */
public record SoftwareRequestDto(@Nullable String title, @Nullable String description, @Nullable LocalDate releaseDate) {
}

package com.archive.backend.software.web.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public record SoftwareDetailsDto(UUID id, String title, @Nullable String description, @Nullable LocalDate releaseDate) {
}

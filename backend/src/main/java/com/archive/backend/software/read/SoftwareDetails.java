package com.archive.backend.software.read;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

/** One software with everything shown about it. */
public record SoftwareDetails(UUID id, String title, @Nullable String description, @Nullable LocalDate releaseDate) {
}

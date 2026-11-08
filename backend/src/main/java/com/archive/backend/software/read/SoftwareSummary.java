package com.archive.backend.software.read;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

/** Software as an entry of a list. */
public record SoftwareSummary(UUID id, String title, @Nullable LocalDate releaseDate) {
}

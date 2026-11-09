package com.archive.backend.software.read;

import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Software as an entry of a list. */
public record SoftwareSummary(
        UUID id, String title, @Nullable LocalDate releaseDate) {}

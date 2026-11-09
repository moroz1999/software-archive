package com.archive.backend.software.read;

import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** One software with everything shown about it. */
public record SoftwareDetails(
        UUID id,
        String title,
        @Nullable String description,
        @Nullable LocalDate releaseDate) {}

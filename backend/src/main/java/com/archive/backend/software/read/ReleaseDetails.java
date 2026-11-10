package com.archive.backend.software.read;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A release as it states itself, to edit it: its own title, if any, and only its own hardware.
 */
public record ReleaseDetails(
        UUID id,
        @Nullable String title,
        @Nullable String version,
        String type,
        @Nullable LocalDate releaseDate,
        @Nullable String description,
        List<SoftwareHardware> hardware) {}

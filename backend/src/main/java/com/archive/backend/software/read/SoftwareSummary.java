package com.archive.backend.software.read;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Software as an entry of a list, with the screenshots its card shows (docs/domain/software.md#screenshots).
 */
public record SoftwareSummary(
        UUID id, String title, @Nullable LocalDate releaseDate, List<SoftwareScreenshot> screenshots) {

    public SoftwareSummary(UUID id, String title, @Nullable LocalDate releaseDate) {
        this(id, title, releaseDate, List.of());
    }
}

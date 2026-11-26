package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Software as an entry of a list, with the screenshots its card shows (docs/domain/software.md#screenshots) and the
 * names of who made it (docs/domain/software.md#card) and its rating (docs/domain/voting.md).
 */
public record SoftwareSummary(
        UUID id,
        String title,
        @Nullable String releaseDate,
        List<SoftwareScreenshot> screenshots,
        List<String> madeBy,
        double rating,
        int votesAmount) {

    public SoftwareSummary(
            UUID id,
            String title,
            @Nullable String releaseDate,
            List<SoftwareScreenshot> screenshots,
            List<String> madeBy) {
        this(id, title, releaseDate, screenshots, madeBy, 0, 0);
    }

    public SoftwareSummary(UUID id, String title, @Nullable String releaseDate) {
        this(id, title, releaseDate, List.of(), List.of());
    }

    public SoftwareSummary(UUID id, String title, @Nullable String releaseDate, List<SoftwareScreenshot> screenshots) {
        this(id, title, releaseDate, screenshots, List.of());
    }
}

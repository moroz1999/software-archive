package com.archive.backend.software.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareReleaseDto(
        UUID id,
        String title,
        @Nullable String version,
        String type,
        @Nullable String releaseDate,
        @Nullable String description,
        List<SoftwareHardwareDto> hardware,
        List<ScreenshotDto> screenshots,
        @Nullable ReleaseArchiveDto archive,
        AddedDto added,
        boolean canChange) {

    /** Who added the release, {@code null} once their account was deleted, and when. */
    public record AddedDto(@Nullable String by, Instant at) {}
}

package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A release as it states itself, to edit it: its own title, if any, only its own hardware, and its credits.
 */
public record ReleaseDetails(
        UUID id,
        @Nullable String title,
        @Nullable String version,
        String type,
        @Nullable String releaseDate,
        @Nullable String description,
        List<SoftwareHardware> hardware,
        @Nullable ReleaseArchiveDetails archive,
        List<SoftwareCredit> credits) {

    public ReleaseDetails(
            UUID id,
            @Nullable String title,
            @Nullable String version,
            String type,
            @Nullable String releaseDate,
            @Nullable String description,
            List<SoftwareHardware> hardware,
            @Nullable ReleaseArchiveDetails archive) {
        this(id, title, version, type, releaseDate, description, hardware, archive, List.of());
    }

    public ReleaseDetails(
            UUID id,
            @Nullable String title,
            @Nullable String version,
            String type,
            @Nullable String releaseDate,
            @Nullable String description,
            List<SoftwareHardware> hardware) {
        this(id, title, version, type, releaseDate, description, hardware, null);
    }
}

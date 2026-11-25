package com.archive.backend.software.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareDetailsDto(
        UUID id,
        String title,
        @Nullable String description,
        @Nullable String instructions,
        @Nullable String releaseDate,
        String preservation,
        SoftwarePlatformDto platform,
        List<SoftwareHardwareDto> hardware,
        List<SoftwareCategoryDto> categories,
        List<SoftwareCreditDto> credits,
        List<SoftwareSourceDto> sources,
        List<OriginDto> origins,
        AddedDto added,
        boolean canChange) {

    public record SoftwareCreditDto(UUID id, @Nullable UUID ownerId, String type, String title, List<String> roles) {}

    /** Who added the entry, {@code null} once their account was deleted, and when. */
    public record AddedDto(@Nullable String by, Instant at) {}

    /** An import the entry came from: the source archive, the id there and its page there, if it has one. */
    public record OriginDto(
            String origin, String externalId, @Nullable String url) {}
}

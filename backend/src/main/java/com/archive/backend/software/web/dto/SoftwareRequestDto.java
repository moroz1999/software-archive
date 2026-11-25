package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing software.
 */
public record SoftwareRequestDto(
        @Nullable String title,
        @Nullable String description,
        @Nullable String instructions,
        @Nullable String releaseDate,
        @Nullable String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> categories,
        @Nullable List<CreditDto> credits,
        @Nullable String preservation) {

    /** A person, a group or the exact label of either, with the roles they had. */
    public record CreditDto(
            @Nullable UUID id,
            @Nullable String type,
            @Nullable List<String> roles) {}
}

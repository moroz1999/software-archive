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
        @Nullable String releaseDate,
        @Nullable String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> categories,
        @Nullable List<SoftwareReferenceRequestDto> publishers,
        @Nullable List<SoftwareReferenceRequestDto> developers,
        @Nullable List<CreditDto> credits) {

    public record CreditDto(
            @Nullable UUID id,
            @Nullable String type,
            @Nullable List<String> roles) {}
}

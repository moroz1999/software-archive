package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareDetailsDto(
        UUID id,
        String title,
        @Nullable String description,
        @Nullable String releaseDate,
        SoftwarePlatformDto platform,
        List<SoftwareHardwareDto> hardware,
        List<SoftwareCategoryDto> categories,
        List<SoftwareCreditDto> credits,
        List<SoftwareSourceDto> sources,
        boolean canChange) {

    public record SoftwareCreditDto(UUID id, @Nullable UUID ownerId, String type, String title, List<String> roles) {}
}

package com.archive.backend.software.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareDetailsDto(
        UUID id,
        String title,
        @Nullable String description,
        @Nullable LocalDate releaseDate,
        SoftwarePlatformDto platform,
        List<SoftwareHardwareDto> hardware,
        List<SoftwareCategoryDto> categories,
        List<SoftwareReferenceDto> publishers,
        List<SoftwareReferenceDto> developers,
        List<SoftwareCreditDto> credits) {

    public record SoftwareCreditDto(UUID id, String type, String title, List<String> roles) {}
}

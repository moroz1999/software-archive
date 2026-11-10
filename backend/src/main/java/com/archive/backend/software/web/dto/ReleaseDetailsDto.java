package com.archive.backend.software.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ReleaseDetailsDto(
        UUID id,
        @Nullable String title,
        @Nullable String version,
        String type,
        @Nullable LocalDate releaseDate,
        @Nullable String description,
        List<SoftwareHardwareDto> hardware) {}

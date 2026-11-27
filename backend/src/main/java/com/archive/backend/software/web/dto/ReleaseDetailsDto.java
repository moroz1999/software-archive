package com.archive.backend.software.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ReleaseDetailsDto(
        UUID id,
        @Nullable String title,
        @Nullable String version,
        @Schema(ref = "ReleaseType") String type,
        @Nullable String releaseDate,
        @Nullable String description,
        List<SoftwareHardwareDto> hardware,
        @Nullable ReleaseArchiveDto archive,
        List<SoftwareDetailsDto.SoftwareCreditDto> credits) {}

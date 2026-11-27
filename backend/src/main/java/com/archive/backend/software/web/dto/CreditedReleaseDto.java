package com.archive.backend.software.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A release on the page of a person, a group or a label: the release, its software, and their credits on it. */
public record CreditedReleaseDto(
        UUID id,
        UUID softwareId,
        String softwareTitle,
        String title,
        @Nullable String version,
        @Schema(ref = "ReleaseType") String type,
        @Nullable String releaseDate,
        List<ScreenshotDto> screenshots,
        List<CreditedSoftwareDto.OwnCreditDto> credits) {}

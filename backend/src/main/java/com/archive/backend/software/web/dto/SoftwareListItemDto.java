package com.archive.backend.software.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareListItemDto(
        UUID id,
        String title,
        @Nullable String releaseDate,
        List<ScreenshotDto> screenshots,
        List<String> madeBy,
        double rating,
        int votesAmount) {}

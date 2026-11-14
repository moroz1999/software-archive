package com.archive.backend.search.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareCatalogItemDto(
        UUID id,
        String title,
        @Nullable String releaseDate,
        List<CardScreenshotDto> screenshots,
        List<String> madeBy) {}

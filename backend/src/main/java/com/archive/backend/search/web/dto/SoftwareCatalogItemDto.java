package com.archive.backend.search.web.dto;

import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record SoftwareCatalogItemDto(
        UUID id, String title, @Nullable LocalDate releaseDate) {}

package com.archive.backend.search.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The conditions of the catalog as the query of its address gives them; platforms and hardware are joined by commas. */
public record SoftwareCatalogParams(
        @Nullable String keyword,
        @Nullable String platform,
        @Nullable Integer year,
        @Nullable UUID category,
        @Nullable UUID country,
        @Nullable UUID city,
        @Nullable List<String> platforms,
        @Nullable List<String> hardware) {}

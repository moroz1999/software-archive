package com.archive.backend.assistant.web.search;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of search_software. */
public record SearchSoftwareArguments(
        @Nullable String keyword,
        @Nullable String platform,
        @Nullable Integer year,
        @Nullable UUID categoryId,
        @Nullable UUID countryId,
        @Nullable UUID cityId,
        @Nullable List<String> platforms,
        @Nullable List<String> hardware,
        @Nullable Integer page,
        @Nullable Integer size) {}

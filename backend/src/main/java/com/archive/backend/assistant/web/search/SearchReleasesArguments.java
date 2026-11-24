package com.archive.backend.assistant.web.search;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of search_releases. */
public record SearchReleasesArguments(
        @Nullable String word,
        @Nullable List<UUID> softwareIds,
        @Nullable List<String> types,
        @Nullable Integer yearFrom,
        @Nullable Integer yearTo,
        @Nullable String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> creditedIds,
        @Nullable List<String> roles,
        @Nullable String fileName,
        @Nullable String md5,
        @Nullable Integer page,
        @Nullable Integer size) {}

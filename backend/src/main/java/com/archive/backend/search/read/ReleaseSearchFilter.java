package com.archive.backend.search.read;

import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The conditions of the release search, as codes and ids; an empty set or null is no condition. */
public record ReleaseSearchFilter(
        @Nullable String word,
        Set<UUID> software,
        Set<String> types,
        @Nullable Integer yearFrom,
        @Nullable Integer yearTo,
        @Nullable String platform,
        Set<String> hardware,
        Set<UUID> credited,
        Set<String> roles,
        @Nullable String fileName,
        @Nullable String md5) {}

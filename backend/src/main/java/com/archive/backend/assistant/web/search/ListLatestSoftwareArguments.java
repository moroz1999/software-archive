package com.archive.backend.assistant.web.search;

import org.jspecify.annotations.Nullable;

/** The arguments of list_latest_software. */
public record ListLatestSoftwareArguments(
        @Nullable String platform,
        @Nullable Integer page,
        @Nullable Integer size) {}

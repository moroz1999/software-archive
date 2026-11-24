package com.archive.backend.assistant.web.search;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** The arguments of search. */
public record SearchArguments(
        String phrase,
        @Nullable List<String> kinds,
        @Nullable String platform,
        @Nullable Integer page) {}

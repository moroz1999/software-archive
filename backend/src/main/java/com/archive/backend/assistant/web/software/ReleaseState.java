package com.archive.backend.assistant.web.software;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** A release as it states itself, in the names of the arguments of its tools: only its own hardware. */
public record ReleaseState(
        @Nullable String title,
        @Nullable String version,
        String type,
        @Nullable String releaseDate,
        @Nullable String description,
        List<String> hardware,
        List<CreditState> credits) {}

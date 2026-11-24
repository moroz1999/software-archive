package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Software as it is, in the names of the arguments of its tools. */
public record SoftwareState(
        String title,
        @Nullable String description,
        @Nullable String releaseDate,
        String platform,
        List<String> hardware,
        List<UUID> categoryIds,
        List<CreditState> credits,
        String preservation) {}

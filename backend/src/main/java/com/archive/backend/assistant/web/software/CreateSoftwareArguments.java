package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of create_software. */
public record CreateSoftwareArguments(
        String title,
        @Nullable String description,
        @Nullable String instructions,
        @Nullable String sourceUrl,
        @Nullable String releaseDate,
        String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> categoryIds,
        @Nullable List<CreditArguments> credits,
        @Nullable String preservation) {}

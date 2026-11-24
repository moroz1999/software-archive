package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of update_software: only the fields given change. */
public record UpdateSoftwareArguments(
        UUID id,
        @Nullable String title,
        @Nullable String description,
        @Nullable String releaseDate,
        @Nullable String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> categoryIds,
        @Nullable List<CreditArguments> credits,
        @Nullable String preservation) {}

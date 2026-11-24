package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of update_release: only the fields given change. */
public record UpdateReleaseArguments(
        UUID softwareId,
        UUID releaseId,
        @Nullable String title,
        @Nullable String version,
        @Nullable String type,
        @Nullable String releaseDate,
        @Nullable String description,
        @Nullable List<String> hardware,
        @Nullable List<CreditArguments> credits) {}

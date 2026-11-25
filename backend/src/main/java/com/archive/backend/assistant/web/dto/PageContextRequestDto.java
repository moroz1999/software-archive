package com.archive.backend.assistant.web.dto;

import org.jspecify.annotations.Nullable;

/** The page a message is written from: its path in the archive, its title, and the entry it shows, if any. */
public record PageContextRequestDto(
        @Nullable String path,
        @Nullable String title,
        @Nullable String entityType,
        @Nullable String entityId) {}

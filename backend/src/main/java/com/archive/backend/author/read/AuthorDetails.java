package com.archive.backend.author.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** One author with everything shown on their page. */
public record AuthorDetails(
        UUID id,
        String name,
        @Nullable String realName,
        @Nullable String notes,
        List<AuthorLabel> labels,
        List<AuthorLocation> locations,
        List<AuthorGroup> groups) {}

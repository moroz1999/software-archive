package com.archive.backend.author.read;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** One author with everything shown on their page. */
public record AuthorDetails(
        UUID id,
        String name,
        @Nullable String realName,
        @Nullable String notes,
        List<AuthorAlias> aliases,
        List<AuthorLocation> locations,
        List<AuthorGroup> groups
) {
}

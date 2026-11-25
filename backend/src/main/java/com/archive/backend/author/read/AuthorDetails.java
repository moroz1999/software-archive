package com.archive.backend.author.read;

import com.archive.backend.shared.read.ExternalOrigin;
import com.archive.backend.user.read.Addition;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One author with everything shown on their page, and where imports took them from.
 */
public record AuthorDetails(
        UUID id,
        String name,
        @Nullable String realName,
        @Nullable String notes,
        List<AuthorLabel> labels,
        List<AuthorLocation> locations,
        List<AuthorGroup> groups,
        List<ExternalOrigin> origins,
        Addition added,
        @Nullable UUID addedBy,
        boolean canChange) {
    /** As read, before the handler tells whether the actor may change it. */
    public AuthorDetails(
            UUID id,
            String name,
            @Nullable String realName,
            @Nullable String notes,
            List<AuthorLabel> labels,
            List<AuthorLocation> locations,
            List<AuthorGroup> groups,
            Addition added) {
        this(id, name, realName, notes, labels, locations, groups, List.of(), added, null, false);
    }

    /** The same person, telling whether the actor may change them. */
    public AuthorDetails withCanChange(boolean canChange) {
        return new AuthorDetails(
                id, name, realName, notes, labels, locations, groups, origins, added, addedBy, canChange);
    }
}

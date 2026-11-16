package com.archive.backend.group.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * An entry of the group catalog: a group under its name, or one of its labels with the group's name next to it
 * (docs/domain/authors.md). It leads to the group or to the label's own page.
 */
public record GroupCatalogEntry(
        @Nullable UUID groupId,
        @Nullable UUID labelId,
        String name,
        @Nullable String note) {

    public GroupCatalogEntry {
        if ((groupId == null) == (labelId == null)) {
            throw new IllegalArgumentException("An entry is of a group or of a label: " + name);
        }
    }
}

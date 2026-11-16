package com.archive.backend.author.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * An entry of the author catalog: a person under their real name with their main label next to it, or a label —
 * theirs with the real name next to it, else the main name it belongs to, or one of nobody (docs/domain/authors.md).
 * It leads to the person or to the label's own page.
 */
public record AuthorCatalogEntry(
        @Nullable UUID personId,
        @Nullable UUID labelId,
        String name,
        @Nullable String note) {

    public AuthorCatalogEntry {
        if ((personId == null) == (labelId == null)) {
            throw new IllegalArgumentException("An entry is of a person or of a label: " + name);
        }
    }
}

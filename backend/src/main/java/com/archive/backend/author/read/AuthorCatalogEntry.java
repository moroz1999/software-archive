package com.archive.backend.author.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * An entry of the author catalog: a person under their main name, or one of their other labels with the main name it
 * belongs to, or a label of nobody. The real name of the person is shown next to the entry, unless it is the entry's
 * name itself.
 */
public record AuthorCatalogEntry(
        @Nullable UUID personId,
        String name,
        @Nullable String realName,
        @Nullable String labelOf,
        @Nullable UUID labelId) {

    public AuthorCatalogEntry {
        if ((personId == null) == (labelId == null)) {
            throw new IllegalArgumentException("An entry is of a person or of a label of nobody: " + name);
        }
    }
}

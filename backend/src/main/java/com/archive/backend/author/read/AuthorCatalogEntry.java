package com.archive.backend.author.read;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * An entry of the author catalog: a person under their main name, or one of their other labels with the main name it
 * belongs to. The real name of the person is shown next to the entry, unless it is the entry's name itself.
 */
public record AuthorCatalogEntry(UUID personId, String name, @Nullable String realName, @Nullable String labelOf) {
}

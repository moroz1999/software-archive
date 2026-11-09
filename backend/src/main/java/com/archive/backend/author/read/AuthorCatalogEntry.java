package com.archive.backend.author.read;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** An entry of the author catalog: a person under their main name, or one of their other aliases with the main name it belongs to. */
public record AuthorCatalogEntry(UUID personId, String name, @Nullable String aliasOf) {
}

package com.archive.backend.search.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A person or a label of nobody found by the search, under the main name; the real name when known and not the main
 * name; the kind, {@code person} or {@code label}, tells which page the match leads to.
 */
public record FoundAuthor(UUID id, String name, @Nullable String realName, String kind) {}

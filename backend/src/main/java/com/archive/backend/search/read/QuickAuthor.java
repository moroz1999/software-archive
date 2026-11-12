package com.archive.backend.search.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A person found by the quick search, under the main name; the real name when known and not the main name. */
public record QuickAuthor(UUID id, String name, @Nullable String realName) {}

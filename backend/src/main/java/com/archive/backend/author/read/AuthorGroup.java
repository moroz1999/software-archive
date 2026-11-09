package com.archive.backend.author.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A group the person was a member of, under the group's main name; {@code as} is the label they were a member under,
 * unless it is their main name.
 */
public record AuthorGroup(UUID id, String name, @Nullable String as) {}

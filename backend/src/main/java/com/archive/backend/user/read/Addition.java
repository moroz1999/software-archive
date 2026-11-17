package com.archive.backend.user.read;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Who added an entry of the archive and when, as its page shows it (docs/domain/users.md#user-groups): {@code by} is
 * the user name, {@code null} once their account was deleted.
 */
public record Addition(@Nullable String by, Instant at) {}

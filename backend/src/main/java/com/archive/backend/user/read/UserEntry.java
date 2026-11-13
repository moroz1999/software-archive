package com.archive.backend.user.read;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * An account in the list of users an administrator keeps.
 */
public record UserEntry(
        UUID id, String name, String email, boolean confirmed, Instant registeredAt, List<String> groups) {}

package com.archive.backend.group.read;

import java.util.UUID;

/**
 * A label of the group; {@code display} marks the one chosen as the display name.
 */
public record GroupLabel(UUID id, String name, boolean display) {}

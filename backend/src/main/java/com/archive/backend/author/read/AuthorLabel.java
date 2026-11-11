package com.archive.backend.author.read;

import java.util.UUID;

/**
 * A label of the author; {@code display} marks the one chosen as the display name.
 */
public record AuthorLabel(UUID id, String name, boolean display) {}

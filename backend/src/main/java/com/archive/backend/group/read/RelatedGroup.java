package com.archive.backend.group.read;

import java.util.UUID;

/**
 * A group inside another one, or one containing it, under its main name.
 */
public record RelatedGroup(UUID id, String name) {}

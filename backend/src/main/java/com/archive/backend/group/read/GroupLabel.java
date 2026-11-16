package com.archive.backend.group.read;

import java.util.UUID;

/** A label of the group: one of its alternative names. */
public record GroupLabel(UUID id, String name) {}

package com.archive.backend.software.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** One person, group or label offered by the software relation lookup. */
public record SoftwareReferenceOption(UUID id, @Nullable UUID ownerId, String type, String title) {}

package com.archive.backend.software.read;

import java.util.UUID;

/** One person, group or label offered by the software relation lookup. */
public record SoftwareReferenceOption(UUID id, String type, String title) {}

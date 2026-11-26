package com.archive.backend.software.read;

import java.util.UUID;

/** A program to link to from another software entry. */
public record SoftwareLink(UUID id, String title) {}

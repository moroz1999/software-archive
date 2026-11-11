package com.archive.backend.software.read;

import java.util.UUID;

/**
 * A category on the branch from the top of the tree down to a category software is in.
 */
public record SoftwareCategoryStep(UUID id, String title) {}

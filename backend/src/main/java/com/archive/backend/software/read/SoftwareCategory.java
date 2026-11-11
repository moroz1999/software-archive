package com.archive.backend.software.read;

import java.util.List;
import java.util.UUID;

/**
 * A category software is in, with its branch from the top of the tree down to itself.
 */
public record SoftwareCategory(UUID id, String title, List<SoftwareCategoryStep> branch) {}

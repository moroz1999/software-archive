package com.archive.backend.category.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One category as its form shows it; no parent at the top of the tree.
 */
public record CategoryDetails(
        UUID id, String title, @Nullable UUID parentId) {}

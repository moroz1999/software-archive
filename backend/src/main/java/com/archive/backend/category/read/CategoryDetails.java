package com.archive.backend.category.read;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One category as its form shows it: its titles by the codes of their languages; no parent at the top of the tree.
 */
public record CategoryDetails(
        UUID id, Map<String, String> titles, @Nullable UUID parentId) {}

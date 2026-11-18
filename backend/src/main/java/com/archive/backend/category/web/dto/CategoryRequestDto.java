package com.archive.backend.category.web.dto;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing a category: its titles by the codes of their languages; no parent puts it at
 * the top of the tree.
 */
public record CategoryRequestDto(
        @Nullable Map<String, String> titles, @Nullable UUID parentId) {}

package com.archive.backend.category.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing a category; no parent puts it at the top of the tree.
 */
public record CategoryRequestDto(
        @Nullable String title, @Nullable UUID parentId) {}

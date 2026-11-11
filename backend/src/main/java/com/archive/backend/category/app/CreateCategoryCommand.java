package com.archive.backend.category.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryTitle;
import org.jspecify.annotations.Nullable;

/**
 * A new category; without a parent it goes to the top of the tree.
 */
public record CreateCategoryCommand(
        CategoryTitle title, @Nullable CategoryId parent) {}

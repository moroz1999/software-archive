package com.archive.backend.category.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryTitle;
import org.jspecify.annotations.Nullable;

public record UpdateCategoryCommand(
        CategoryId id, CategoryTitle title, @Nullable CategoryId parent) {}

package com.archive.backend.category.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryTitles;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

public record UpdateCategoryCommand(
        Actor actor,
        CategoryId id,
        CategoryTitles titles,
        @Nullable CategoryId parent) {}

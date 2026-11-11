package com.archive.backend.category.app;

import com.archive.backend.category.domain.CategoryId;

public record DeleteCategoryCommand(CategoryId id) {}

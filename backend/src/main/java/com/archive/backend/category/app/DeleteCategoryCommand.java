package com.archive.backend.category.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.user.domain.Actor;

public record DeleteCategoryCommand(Actor actor, CategoryId id) {}

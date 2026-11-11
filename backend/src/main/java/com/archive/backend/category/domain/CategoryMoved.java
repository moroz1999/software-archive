package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/**
 * A category went under another parent or to the top: the categories above it, and above its subcategories, changed.
 */
public record CategoryMoved(CategoryId category) implements DomainEvent {}

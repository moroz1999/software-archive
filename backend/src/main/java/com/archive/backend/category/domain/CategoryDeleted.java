package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;

/**
 * A category is being deleted with its subcategories: the software in them leaves them. Told before the deletion, while
 * the subtree can still be found.
 */
public record CategoryDeleted(CategoryId category) implements DomainEvent {}

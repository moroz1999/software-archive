package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A category cannot be put under itself or under one of its own subcategories (docs/domain/categories.md).
 */
public class CategoryParentInsideException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CategoryParentInsideException() {
        super("category.parent-inside", "A category cannot be put under itself or its subcategory.", "parentId");
    }
}

package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * The parent chosen for a category is not in the archive.
 */
public class CategoryParentUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CategoryParentUnknownException() {
        super("category.parent-unknown", "The parent category is not in the archive.", "parentId");
    }
}

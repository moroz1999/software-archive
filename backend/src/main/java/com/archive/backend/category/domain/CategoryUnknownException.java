package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A category chosen for software that is not in the archive.
 */
public class CategoryUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CategoryUnknownException() {
        super("category.unknown", "The category is not in the archive.", "categories");
    }
}

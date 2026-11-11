package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public CategoryNotFoundException() {
        super("category.not-found", "Category not found.");
    }
}

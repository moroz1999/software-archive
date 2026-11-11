package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.Title;
import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * The title of a category; categories under different parents may share one.
 */
@ValueObject
public final class CategoryTitle extends Title {

    public CategoryTitle(String value) {
        super(value);
    }
}

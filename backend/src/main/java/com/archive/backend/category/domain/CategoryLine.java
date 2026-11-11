package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;

/**
 * A category with the categories above it in the tree, its parent first and the top last; a category at the top has
 * none.
 */
@ValueObject
public record CategoryLine(CategoryId category, List<CategoryId> ancestors) {

    public CategoryLine {
        Objects.requireNonNull(category, "category");
        ancestors = List.copyOf(ancestors);
    }
}

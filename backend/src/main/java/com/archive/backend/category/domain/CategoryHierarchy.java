package com.archive.backend.category.domain;

import java.util.List;
import java.util.Optional;

/**
 * The tree of categories, the parent of a category is checked against.
 */
@FunctionalInterface
public interface CategoryHierarchy {

    /**
     * The category and its ancestors up to the top of the tree; nothing for a category that is not in the archive.
     */
    Optional<List<CategoryId>> lineOf(CategoryId category);
}

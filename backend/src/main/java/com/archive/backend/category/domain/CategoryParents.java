package com.archive.backend.category.domain;

/**
 * Builds the parent of a category, checking it against the tree: it is in the archive, and the category is not on
 * its line, so a category never goes under itself or its own subcategory.
 */
public class CategoryParents {

    private final CategoryHierarchy hierarchy;

    public CategoryParents(CategoryHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public CategoryParent of(CategoryId category, CategoryId parent) {
        if (hierarchy
                .lineOf(parent)
                .orElseThrow(CategoryParentUnknownException::new)
                .contains(category)) {
            throw new CategoryParentInsideException();
        }
        return new CategoryParent(category, parent);
    }
}

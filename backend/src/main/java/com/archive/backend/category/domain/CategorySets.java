package com.archive.backend.category.domain;

import java.util.List;
import java.util.Set;

/**
 * Builds the categories of something from the lines the archive has for them.
 */
public class CategorySets {

    private final CategoryCatalog catalog;

    public CategorySets(CategoryCatalog catalog) {
        this.catalog = catalog;
    }

    public CategorySet of(Set<CategoryId> ids) {
        List<CategoryLine> lines = catalog.linesOf(ids);
        if (lines.size() != ids.size()) {
            throw new CategoryUnknownException();
        }
        return new CategorySet(lines);
    }
}

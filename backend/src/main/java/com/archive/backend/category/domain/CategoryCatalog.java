package com.archive.backend.category.domain;

import java.util.List;
import java.util.Set;

/**
 * The categories of the archive, the categories of software are looked up in.
 */
@FunctionalInterface
public interface CategoryCatalog {

    /**
     * The lines of the given categories the archive has; a category it does not have has none.
     */
    List<CategoryLine> linesOf(Set<CategoryId> categories);
}

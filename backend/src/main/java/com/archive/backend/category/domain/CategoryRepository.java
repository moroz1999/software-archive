package com.archive.backend.category.domain;

import java.util.Optional;

/**
 * Categories as domain entities, for the use cases that change them. Reads go through the read model.
 */
public interface CategoryRepository {

    Optional<Category> findById(CategoryId id);

    /**
     * Stores the category; one new or moved under another parent goes last among its children.
     */
    void save(Category category);

    /**
     * Deletes the category with all its subcategories; their software only leaves them.
     */
    void delete(Category category);
}

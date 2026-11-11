package com.archive.backend.category.read;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reads categories as plain read models (docs/backend.md).
 */
public interface CategoryReadRepository {

    /**
     * The whole tree: the categories at its top, each with its subcategories, in their order.
     */
    List<CategoryNode> findTree();

    /**
     * A category; nothing for an unknown one.
     */
    Optional<CategoryDetails> findById(UUID id);
}

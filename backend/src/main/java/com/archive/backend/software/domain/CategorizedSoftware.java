package com.archive.backend.software.domain;

import com.archive.backend.category.domain.CategoryId;
import java.util.List;

/**
 * Finds the software a change of the tree has put in a category together with one above it.
 */
@FunctionalInterface
public interface CategorizedSoftware {

    /**
     * The software in the moved category or one of its subcategories and in a category now above the moved one, in the
     * order of its ids.
     */
    List<SoftwareId> inCategoriesAboveOthersAfterMoving(CategoryId moved);
}

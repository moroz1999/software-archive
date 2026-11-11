package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The parent of one category: in the archive and outside the category's own subtree. Only {@link CategoryParents}
 * builds one, checking it for that category; storage restores what was checked.
 */
@ValueObject
public final class CategoryParent {

    private final CategoryId category;
    private final CategoryId parent;

    CategoryParent(CategoryId category, CategoryId parent) {
        this.category = Objects.requireNonNull(category, "category");
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    /**
     * A parent read back from storage: it was checked when stored.
     */
    public static CategoryParent restore(CategoryId category, CategoryId parent) {
        return new CategoryParent(category, parent);
    }

    /**
     * The category the parent was checked for.
     */
    public CategoryId category() {
        return category;
    }

    public CategoryId id() {
        return parent;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof CategoryParent that && that.category.equals(category) && that.parent.equals(parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, parent);
    }

    @Override
    public String toString() {
        return "CategoryParent[" + parent.value() + " of " + category.value() + "]";
    }
}

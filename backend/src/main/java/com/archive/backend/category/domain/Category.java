package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * A category of software in the one tree of every platform (docs/domain/categories.md). Without a parent it is at the
 * top of the tree.
 */
@Getter
public final class Category {

    private final CategoryId id;
    private CategoryTitle title;
    private @Nullable CategoryParent parent;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Category(CategoryId id, CategoryTitle title, @Nullable CategoryParent parent) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.parent = checkedFor(id, parent);
    }

    public static Category create(CategoryId id, CategoryTitle title, @Nullable CategoryParent parent) {
        return new Category(id, title, parent);
    }

    /**
     * A category put under another parent, or to or from the top, tells it moved.
     */
    public void edit(CategoryTitle title, @Nullable CategoryParent parent) {
        this.title = Objects.requireNonNull(title, "title");
        CategoryParent checked = checkedFor(id, parent);
        if (!Objects.equals(parentId(this.parent), parentId(checked))) {
            events.add(new CategoryMoved(id));
        }
        this.parent = checked;
    }

    private static @Nullable CategoryId parentId(@Nullable CategoryParent parent) {
        return parent == null ? null : parent.id();
    }

    /** Tells the category is going with its subcategories: its repository deletes them. */
    public void delete() {
        events.add(new CategoryDeleted(id));
    }

    /**
     * What happened to the category since it was loaded, handed over once.
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /**
     * A parent is checked for one category: taking one checked for another would skip its check.
     */
    private static @Nullable CategoryParent checkedFor(CategoryId id, @Nullable CategoryParent parent) {
        if (parent != null && !parent.category().equals(id)) {
            throw new IllegalArgumentException("The parent was checked for another category.");
        }
        return parent;
    }
}

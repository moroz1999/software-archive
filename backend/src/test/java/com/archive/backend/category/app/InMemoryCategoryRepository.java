package com.archive.backend.category.app;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryHierarchy;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** The categories the use case tests work on; the tree they form is the hierarchy their parents are checked against. */
class InMemoryCategoryRepository implements CategoryRepository, CategoryHierarchy {

    private final Map<CategoryId, Category> items = new LinkedHashMap<>();

    int count() {
        return items.size();
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void save(Category category) {
        items.put(category.getId(), category);
    }

    /** Removes the category with its subtree, as the schema does. */
    @Override
    public void delete(Category category) {
        List<CategoryId> subtree = items.keySet().stream()
                .filter(id -> lineOf(id).orElseThrow().contains(category.getId()))
                .toList();
        subtree.forEach(items::remove);
    }

    @Override
    public Optional<List<CategoryId>> lineOf(CategoryId category) {
        if (!items.containsKey(category)) {
            return Optional.empty();
        }
        List<CategoryId> line = new ArrayList<>();
        @Nullable CategoryId current = category;
        while (current != null) {
            line.add(current);
            CategoryParent parent = Objects.requireNonNull(items.get(current)).getParent();
            current = parent == null ? null : parent.id();
        }
        return Optional.of(line);
    }
}

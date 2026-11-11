package com.archive.backend.category.app;

import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.read.CategoryReadRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Serves what it was given: building the tree is the SQL's job. */
class InMemoryCategoryReadRepository implements CategoryReadRepository {

    private final List<CategoryNode> tree = new ArrayList<>();
    private final List<CategoryDetails> details = new ArrayList<>();

    void add(CategoryNode node) {
        tree.add(node);
    }

    void add(CategoryDetails category) {
        details.add(category);
    }

    @Override
    public List<CategoryNode> findTree() {
        return List.copyOf(tree);
    }

    @Override
    public Optional<CategoryDetails> findById(UUID id) {
        return details.stream().filter(category -> category.id().equals(id)).findFirst();
    }
}

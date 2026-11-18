package com.archive.backend.category.app;

import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.read.CategoryReadRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Serves what it was given: building the tree and titling it in a language are the SQL's job. */
class InMemoryCategoryReadRepository implements CategoryReadRepository {

    private final Map<String, List<CategoryNode>> trees = new HashMap<>();
    private final List<CategoryDetails> details = new ArrayList<>();

    void add(String language, CategoryNode node) {
        trees.computeIfAbsent(language, code -> new ArrayList<>()).add(node);
    }

    void add(CategoryDetails category) {
        details.add(category);
    }

    @Override
    public List<CategoryNode> findTree(String language) {
        return List.copyOf(trees.getOrDefault(language, List.of()));
    }

    @Override
    public Optional<CategoryDetails> findById(UUID id) {
        return details.stream().filter(category -> category.id().equals(id)).findFirst();
    }
}

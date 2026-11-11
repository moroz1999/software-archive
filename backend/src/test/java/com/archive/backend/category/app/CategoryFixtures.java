package com.archive.backend.category.app;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryTitle;
import java.util.UUID;

/** The tree the category use case tests work on: Games → Action → Shooters, and Demoscene beside them. */
final class CategoryFixtures {

    static final CategoryId GAMES = id(1);
    static final CategoryId ACTION = id(2);
    static final CategoryId SHOOTERS = id(3);
    static final CategoryId DEMOSCENE = id(4);

    private CategoryFixtures() {}

    static CategoryId id(int n) {
        return new CategoryId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }

    static InMemoryCategoryRepository tree() {
        InMemoryCategoryRepository repository = new InMemoryCategoryRepository();
        repository.save(new Category(GAMES, new CategoryTitle("Games"), null));
        repository.save(new Category(ACTION, new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES)));
        repository.save(
                new Category(SHOOTERS, new CategoryTitle("Shooters"), CategoryParent.restore(SHOOTERS, ACTION)));
        repository.save(new Category(DEMOSCENE, new CategoryTitle("Demoscene"), null));
        return repository;
    }
}

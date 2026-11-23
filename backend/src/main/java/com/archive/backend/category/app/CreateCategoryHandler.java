package com.archive.backend.category.app;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParents;
import com.archive.backend.category.domain.CategoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryHandler {

    private final CategoryRepository repository;
    private final CategoryParents parents;

    @Transactional
    public CategoryId handle(CreateCategoryCommand c) {
        Category.requireKeptBy(c.actor());
        CategoryId id = new CategoryId(UUID.randomUUID());
        CategoryId parent = c.parent();
        Category category = Category.create(id, c.titles(), parent == null ? null : parents.of(id, parent));
        repository.save(category);
        return category.getId();
    }
}

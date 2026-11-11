package com.archive.backend.category.app;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryNotFoundException;
import com.archive.backend.category.domain.CategoryParents;
import com.archive.backend.category.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryHandler {

    private final CategoryRepository repository;
    private final CategoryParents parents;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(UpdateCategoryCommand c) {
        Category category = repository.findById(c.id()).orElseThrow(CategoryNotFoundException::new);
        CategoryId parent = c.parent();
        category.edit(c.title(), parent == null ? null : parents.of(c.id(), parent));
        repository.save(category);
        // The software of the category answers within this transaction (docs/backend.md).
        category.pullEvents().forEach(events::publishEvent);
    }
}

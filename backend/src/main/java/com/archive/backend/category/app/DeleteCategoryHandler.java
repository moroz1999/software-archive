package com.archive.backend.category.app;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryNotFoundException;
import com.archive.backend.category.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryHandler {

    private final CategoryRepository repository;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(DeleteCategoryCommand c) {
        Category category = repository.findById(c.id()).orElseThrow(CategoryNotFoundException::new);
        category.delete();
        // Told before the deletion, while what the answers look up is still there (docs/backend.md).
        category.pullEvents().forEach(events::publishEvent);
        repository.delete(category);
    }
}

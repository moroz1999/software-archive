package com.archive.backend.category.app;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryNotFoundException;
import com.archive.backend.category.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryHandler {

    private final CategoryRepository repository;

    @Transactional
    public void handle(DeleteCategoryCommand c) {
        Category category = repository.findById(c.id()).orElseThrow(CategoryNotFoundException::new);
        repository.delete(category);
    }
}

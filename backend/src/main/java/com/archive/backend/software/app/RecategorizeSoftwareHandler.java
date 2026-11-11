package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.software.domain.CategorizedSoftware;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A moved category takes the software in it out of every category that came above another of its own
 * (docs/domain/categories.md): loaded with the lines of the tree as they are now, the software keeps only the deepest
 * category of each branch, and is stored so.
 */
@Service
@RequiredArgsConstructor
public class RecategorizeSoftwareHandler {

    private final SoftwareRepository repository;
    private final CategorizedSoftware categorized;

    @EventListener
    public void on(CategoryMoved event) {
        categorized
                .inCategoriesAboveOthersAfterMoving(event.category())
                .forEach(id -> repository.findById(id).ifPresent(repository::save));
    }
}

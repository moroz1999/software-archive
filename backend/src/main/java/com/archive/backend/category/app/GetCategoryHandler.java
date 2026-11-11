package com.archive.backend.category.app;

import com.archive.backend.category.domain.CategoryNotFoundException;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCategoryHandler {

    private final CategoryReadRepository reads;

    @Transactional(readOnly = true)
    public CategoryDetails handle(GetCategoryQuery q) {
        return reads.findById(q.id().value()).orElseThrow(CategoryNotFoundException::new);
    }
}

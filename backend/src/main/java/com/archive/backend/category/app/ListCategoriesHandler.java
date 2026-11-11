package com.archive.backend.category.app;

import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.read.CategoryReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListCategoriesHandler {

    private final CategoryReadRepository reads;

    @Transactional(readOnly = true)
    public List<CategoryNode> handle(ListCategoriesQuery q) {
        return reads.findTree();
    }
}

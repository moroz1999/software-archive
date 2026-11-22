package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.basis.BasisCategory;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The categories of "Базис" an administrator can import one by one. */
@Service
public class ListBasisCategoriesHandler {

    @Transactional(readOnly = true)
    public List<String> handle(ListBasisCategoriesQuery q) {
        q.actor().administrator();
        return BasisCategory.importable().stream()
                .map(category -> category.code())
                .toList();
    }
}

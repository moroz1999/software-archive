package com.archive.backend.search.app;

import com.archive.backend.search.read.SoftwareCatalog;
import com.archive.backend.search.read.SoftwareCatalogFilter;
import com.archive.backend.search.read.SoftwareCatalogPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A page of the software catalog with the values of its filters (docs/domain/search.md#software-catalog). */
@Service
@RequiredArgsConstructor
public class ListSoftwareCatalogHandler {

    private final SoftwareCatalog catalog;

    @Transactional(readOnly = true)
    public SoftwareCatalogPage handle(ListSoftwareCatalogQuery q) {
        return catalog.find(
                new SoftwareCatalogFilter(
                        q.keyword() == null ? null : q.keyword().value(),
                        q.platform() == null ? null : q.platform().value(),
                        q.year() == null ? null : q.year().value(),
                        q.category() == null ? null : q.category().value(),
                        q.country() == null ? null : q.country().value()),
                q.pageable());
    }
}

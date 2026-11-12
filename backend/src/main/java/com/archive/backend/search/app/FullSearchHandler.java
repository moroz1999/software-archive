package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.read.FullSearch;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The full search (docs/domain/search.md#full-search): a page of a section, nothing without a word. */
@Service
@RequiredArgsConstructor
public class FullSearchHandler {

    private final FullSearch search;

    @Transactional(readOnly = true)
    public Page<FoundAuthor> authors(FullSearchQuery query) {
        CatalogKeyword keyword = query.keyword();
        return keyword == null
                ? Page.empty(query.pageable())
                : search.authors(keyword.value(), platform(query), query.pageable());
    }

    @Transactional(readOnly = true)
    public Page<FoundGroup> groups(FullSearchQuery query) {
        CatalogKeyword keyword = query.keyword();
        return keyword == null
                ? Page.empty(query.pageable())
                : search.groups(keyword.value(), platform(query), query.pageable());
    }

    private static @Nullable String platform(FullSearchQuery query) {
        PlatformCode platform = query.platform();
        return platform == null ? null : platform.value();
    }
}

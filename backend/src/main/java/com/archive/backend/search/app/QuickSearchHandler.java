package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.QuickSearch;
import com.archive.backend.search.read.QuickSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The quick search (docs/domain/search.md#quick-search): the first matches of every section, nothing without a word. */
@Service
@RequiredArgsConstructor
public class QuickSearchHandler {

    /** The matches shown in a section. */
    private static final int HITS = 5;

    private final QuickSearch search;

    @Transactional(readOnly = true)
    public QuickSearchResult handle(QuickSearchQuery query) {
        CatalogKeyword keyword = query.keyword();
        if (keyword == null) {
            return QuickSearchResult.empty();
        }
        PlatformCode platform = query.platform();
        return search.find(keyword.value(), platform == null ? null : platform.value(), HITS);
    }
}

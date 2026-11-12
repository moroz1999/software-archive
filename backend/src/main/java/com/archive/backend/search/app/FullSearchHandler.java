package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.FoundType;
import com.archive.backend.search.read.FullSearch;
import com.archive.backend.search.read.FullSearchResult;
import java.util.EnumSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The full search (docs/domain/search.md#full-search): a page of the matches of every kind, nothing without a word. */
@Service
@RequiredArgsConstructor
public class FullSearchHandler {

    /** The matches on one page, of every kind together. */
    static final int PAGE_SIZE = 50;

    private final FullSearch search;

    @Transactional(readOnly = true)
    public FullSearchResult handle(FullSearchQuery query) {
        CatalogKeyword keyword = query.keyword();
        int page = query.pageable().getPageNumber();
        if (keyword == null) {
            return FullSearchResult.empty(page, PAGE_SIZE);
        }
        PlatformCode platform = query.platform();
        return search.find(
                keyword.value(),
                platform == null ? null : platform.value(),
                query.kinds().isEmpty()
                        ? EnumSet.allOf(FoundType.class)
                        : query.kinds().stream()
                                .map(kind -> FoundType.valueOf(kind.name()))
                                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FoundType.class))),
                page,
                PAGE_SIZE);
    }
}

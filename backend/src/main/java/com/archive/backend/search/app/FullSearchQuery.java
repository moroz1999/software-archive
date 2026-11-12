package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.FoundKind;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/**
 * A page of the full search: the keyword, none for a blank one; the platform of the switch; the kinds chosen, every
 * kind when none is; the page asked, its size fixed by the search.
 */
public record FullSearchQuery(
        @Nullable CatalogKeyword keyword, @Nullable PlatformCode platform, Set<FoundKind> kinds, Pageable pageable) {

    public FullSearchQuery {
        kinds = Set.copyOf(kinds);
    }
}

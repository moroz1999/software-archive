package com.archive.backend.search.read;

import java.util.List;

/**
 * A page of the full search: one list of the matches of every kind asked, in the order of the kinds, cut into pages
 * of {@code size}; on the page, the matches of each kind present with the count of all of them.
 */
public record FullSearchResult(int number, int size, long total, List<FoundSet> sets) {

    public FullSearchResult {
        sets = List.copyOf(sets);
    }

    public static FullSearchResult empty(int number, int size) {
        return new FullSearchResult(number, size, 0, List.of());
    }

    public long totalPages() {
        return size == 0 ? 0 : (total + size - 1) / size;
    }
}

package com.archive.backend.search.read;

import java.util.List;

/** The first matches of a section of the quick search and the count of all of them. */
public record QuickSection<T>(List<T> hits, long total) {

    public QuickSection {
        hits = List.copyOf(hits);
    }

    public static <T> QuickSection<T> empty() {
        return new QuickSection<>(List.of(), 0);
    }
}

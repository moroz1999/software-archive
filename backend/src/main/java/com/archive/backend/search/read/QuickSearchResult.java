package com.archive.backend.search.read;

/** The sections of the quick search (docs/domain/search.md#quick-search). */
public record QuickSearchResult(
        QuickSection<SoftwareCatalogItem> software,
        QuickSection<QuickAuthor> authors,
        QuickSection<QuickGroup> groups) {

    public static QuickSearchResult empty() {
        return new QuickSearchResult(QuickSection.empty(), QuickSection.empty(), QuickSection.empty());
    }
}

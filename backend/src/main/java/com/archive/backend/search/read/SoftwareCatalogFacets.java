package com.archive.backend.search.read;

import java.util.List;

/**
 * The values each filter of the catalog offers, counted over the whole result; an active filter's values are counted
 * without its own condition (docs/domain/search.md#software-catalog).
 */
public record SoftwareCatalogFacets(List<YearCount> years, List<IdCount> categories, List<IdCount> countries) {

    public SoftwareCatalogFacets {
        years = List.copyOf(years);
        categories = List.copyOf(categories);
        countries = List.copyOf(countries);
    }
}

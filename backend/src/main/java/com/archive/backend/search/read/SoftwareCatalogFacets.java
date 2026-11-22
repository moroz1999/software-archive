package com.archive.backend.search.read;

import java.util.List;

/**
 * The values each filter of the catalog offers, counted over the whole result; an active filter's values are counted
 * without its own condition (docs/domain/search.md#software-catalog). Categories are no filter but the way through the
 * catalog, offered whole and uncounted.
 */
public record SoftwareCatalogFacets(
        List<YearCount> years,
        List<IdCount> countries,
        List<IdCount> cities,
        List<CodeCount> platforms,
        List<CodeCount> hardware) {

    public SoftwareCatalogFacets {
        years = List.copyOf(years);
        countries = List.copyOf(countries);
        cities = List.copyOf(cities);
        platforms = List.copyOf(platforms);
        hardware = List.copyOf(hardware);
    }

    public SoftwareCatalogFacets(List<YearCount> years, List<IdCount> countries) {
        this(years, countries, List.of(), List.of(), List.of());
    }
}

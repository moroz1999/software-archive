package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalog;
import com.archive.backend.search.read.SoftwareCatalogFacets;
import com.archive.backend.search.read.SoftwareCatalogFilter;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.typesense.api.Client;
import org.typesense.model.FacetCounts;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchResultItem;
import org.typesense.model.MultiSearchSearchesParameter;

/**
 * The software catalog in one {@code multi_search}: the page with the values of the filters not chosen, and for each
 * chosen filter a search of its values alone, without its own condition (docs/features/search.md#reads).
 */
@Repository
@RequiredArgsConstructor
public class TypesenseSoftwareCatalog implements SoftwareCatalog {

    /** Above the count of categories and of countries: every value of a filter is offered. */
    private static final int ALL_VALUES = 1000;

    private final Client client;

    /** A filter of the catalog: its field and its chosen value, if any. */
    private enum Facet {
        YEARS("year"),
        CATEGORIES("categories"),
        COUNTRIES("countries");

        private final String field;

        Facet(String field) {
            this.field = field;
        }

        @Nullable
        Object chosen(SoftwareCatalogFilter filter) {
            return switch (this) {
                case YEARS -> filter.year();
                case CATEGORIES -> filter.category();
                case COUNTRIES -> filter.country();
            };
        }
    }

    @Override
    public SoftwareCatalogPage find(SoftwareCatalogFilter filter, Pageable pageable) {
        List<Facet> chosen = new ArrayList<>();
        List<Facet> open = new ArrayList<>();
        for (Facet facet : Facet.values()) {
            (facet.chosen(filter) == null ? open : chosen).add(facet);
        }

        List<MultiSearchCollectionParameters> searches = new ArrayList<>();
        MultiSearchCollectionParameters page = search(filter, null);
        page.setSortBy("title_sort:asc");
        page.setPage(pageable.getPageNumber() + 1);
        page.setPerPage(pageable.getPageSize());
        facetBy(page, open);
        searches.add(page);
        for (Facet facet : chosen) {
            MultiSearchCollectionParameters values = search(filter, facet);
            values.setPerPage(0);
            facetBy(values, List.of(facet));
            searches.add(values);
        }

        List<MultiSearchResultItem> results = call(
                "search the software catalog",
                () -> client.multiSearch
                        .perform(new MultiSearchSearchesParameter().searches(searches), Map.of())
                        .getResults());
        results.forEach(TypesenseSoftwareCatalog::requireAnswered);

        MultiSearchResultItem items = results.getFirst();
        Map<Facet, List<FacetCounts>> counts = new EnumMap<>(Facet.class);
        open.forEach(facet -> counts.put(facet, items.getFacetCounts()));
        for (int i = 0; i < chosen.size(); i++) {
            counts.put(chosen.get(i), results.get(i + 1).getFacetCounts());
        }

        return new SoftwareCatalogPage(
                new PageImpl<>(
                        items.getHits().stream()
                                .map(hit -> item(hit.getDocument()))
                                .toList(),
                        pageable,
                        items.getFound()),
                new SoftwareCatalogFacets(
                        values(
                                counts,
                                Facet.YEARS,
                                value -> new YearCount(Integer.parseInt(value.value()), value.count())),
                        values(
                                counts,
                                Facet.CATEGORIES,
                                value -> new IdCount(UUID.fromString(value.value()), value.count())),
                        values(
                                counts,
                                Facet.COUNTRIES,
                                value -> new IdCount(UUID.fromString(value.value()), value.count()))));
    }

    /** A search of the catalog with every condition of the filter but the one of {@code without}. */
    private static MultiSearchCollectionParameters search(SoftwareCatalogFilter filter, @Nullable Facet without) {
        MultiSearchCollectionParameters search = new MultiSearchCollectionParameters().collection("software");
        search.setQ(filter.keyword() == null ? "*" : filter.keyword());
        search.setQueryBy("title,alternative_titles");
        List<String> conditions = new ArrayList<>();
        if (filter.platform() != null) {
            conditions.add("platform:=`" + filter.platform() + "`");
        }
        for (Facet facet : Facet.values()) {
            Object value = facet.chosen(filter);
            if (facet != without && value != null) {
                // A number goes bare; a string in backticks, so nothing in it reads as syntax.
                conditions.add(facet.field + ":=" + (value instanceof Integer ? value : "`" + value + "`"));
            }
        }
        if (!conditions.isEmpty()) {
            search.setFilterBy(String.join(" && ", conditions));
        }
        return search;
    }

    private static void facetBy(MultiSearchCollectionParameters search, List<Facet> facets) {
        if (!facets.isEmpty()) {
            search.setFacetBy(String.join(",", facets.stream().map(f -> f.field).toList()));
            search.setMaxFacetValues(ALL_VALUES);
        }
    }

    private static void requireAnswered(MultiSearchResultItem result) {
        if (result.getError() != null) {
            throw new SearchIndexException("Typesense refused a search of the software catalog: " + result.getError());
        }
    }

    private record Value(String value, long count) {}

    private static <T> List<T> values(Map<Facet, List<FacetCounts>> counts, Facet facet, Function<Value, T> read) {
        List<FacetCounts> of = counts.getOrDefault(facet, List.of());
        return of == null
                ? List.of()
                : of.stream()
                        .filter(c -> facet.field.equals(c.getFieldName()))
                        .flatMap(c -> c.getCounts().stream())
                        .map(c -> read.apply(new Value(c.getValue(), c.getCount())))
                        .toList();
    }

    /** The card of software from its document. */
    static SoftwareCatalogItem item(Map<String, Object> doc) {
        Object releaseDate = doc.get("release_date");
        return new SoftwareCatalogItem(
                UUID.fromString(String.valueOf(doc.get("id"))),
                String.valueOf(doc.get("title")),
                releaseDate == null ? null : LocalDate.parse((String) releaseDate));
    }
}

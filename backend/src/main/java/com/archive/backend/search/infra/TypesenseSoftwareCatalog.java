package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.read.CodeCount;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalog;
import com.archive.backend.search.read.SoftwareCatalogFacets;
import com.archive.backend.search.read.SoftwareCatalogFilter;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import java.util.ArrayList;
import java.util.HashMap;
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
 * The software catalog of a category in one {@code multi_search}: the page with the values of the filters not chosen,
 * and for each chosen filter a search of its values alone, without its own condition (docs/features/search.md#reads).
 */
@Repository
@RequiredArgsConstructor
public class TypesenseSoftwareCatalog implements SoftwareCatalog {

    /** Above the count of categories, of countries, of cities and of hardware: every value of a filter is offered. */
    private static final int ALL_VALUES = 1000;

    private final Client client;

    /** A filter of the catalog: the field it is counted by, and its condition while a value is chosen. */
    private record Facet(String field, @Nullable String condition) {

        boolean chosen() {
            return condition != null;
        }
    }

    /** A string in backticks, so nothing in it reads as syntax. */
    private static String quoted(Object value) {
        return "`" + value + "`";
    }

    private static @Nullable String anyOf(String field, List<String> values) {
        return values.isEmpty()
                ? null
                : field + ":=["
                        + String.join(
                                ",",
                                values.stream()
                                        .map(TypesenseSoftwareCatalog::quoted)
                                        .toList()) + "]";
    }

    private static @Nullable String equal(String field, @Nullable Object value) {
        return value == null ? null : field + ":=" + quoted(value);
    }

    /** Every filter of the catalog with the condition the filter chose for it. */
    private static List<Facet> facets(SoftwareCatalogFilter filter) {
        List<Facet> facets = new ArrayList<>();
        facets.add(new Facet("year", filter.year() == null ? null : "year:=" + filter.year()));
        facets.add(new Facet("countries", equal("countries", filter.country())));
        facets.add(new Facet("cities", equal("cities", filter.city())));
        facets.add(new Facet("platform", anyOf("platform", filter.platforms())));
        for (String group : SoftwareDocument.HARDWARE_GROUPS) {
            String field = TypesenseSearchIndex.hardwareField(group);
            facets.add(new Facet(field, anyOf(field, filter.hardware().getOrDefault(group, List.of()))));
        }
        return facets;
    }

    @Override
    public SoftwareCatalogPage find(SoftwareCatalogFilter filter, Pageable pageable) {
        List<Facet> facets = facets(filter);
        List<Facet> chosen = facets.stream().filter(Facet::chosen).toList();
        List<Facet> open = facets.stream().filter(facet -> !facet.chosen()).toList();

        List<MultiSearchCollectionParameters> searches = new ArrayList<>();
        MultiSearchCollectionParameters page = search(filter, facets, null);
        page.setSortBy("title_sort:asc");
        page.setPage(pageable.getPageNumber() + 1);
        page.setPerPage(pageable.getPageSize());
        facetBy(page, open);
        searches.add(page);
        for (Facet facet : chosen) {
            MultiSearchCollectionParameters values = search(filter, facets, facet);
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
        Map<String, List<FacetCounts>> counts = new HashMap<>();
        open.forEach(facet -> counts.put(facet.field(), items.getFacetCounts()));
        for (int i = 0; i < chosen.size(); i++) {
            counts.put(chosen.get(i).field(), results.get(i + 1).getFacetCounts());
        }

        List<CodeCount> hardware = new ArrayList<>();
        for (String group : SoftwareDocument.HARDWARE_GROUPS) {
            hardware.addAll(values(counts, TypesenseSearchIndex.hardwareField(group), TypesenseSoftwareCatalog::code));
        }
        return new SoftwareCatalogPage(
                new PageImpl<>(
                        items.getHits().stream()
                                .map(hit -> item(hit.getDocument()))
                                .toList(),
                        pageable,
                        items.getFound()),
                new SoftwareCatalogFacets(
                        values(counts, "year", value -> new YearCount(Integer.parseInt(value.value()), value.count())),
                        values(counts, "countries", TypesenseSoftwareCatalog::id),
                        values(counts, "cities", TypesenseSoftwareCatalog::id),
                        values(counts, "platform", TypesenseSoftwareCatalog::code),
                        hardware));
    }

    private static IdCount id(Value value) {
        return new IdCount(UUID.fromString(value.value()), value.count());
    }

    private static CodeCount code(Value value) {
        return new CodeCount(value.value(), value.count());
    }

    /** A search of the catalog with every condition of the filter but the one of {@code without}. */
    private static MultiSearchCollectionParameters search(
            SoftwareCatalogFilter filter, List<Facet> facets, @Nullable Facet without) {
        MultiSearchCollectionParameters search = new MultiSearchCollectionParameters().collection("software");
        search.setQ(filter.keyword() == null ? "*" : filter.keyword());
        search.setQueryBy("title,alternative_titles");
        List<String> conditions = new ArrayList<>();
        String platform = equal("platform", filter.platform());
        if (platform != null) {
            conditions.add(platform);
        }
        // The category is where the catalog is, not a filter: every search keeps it.
        String category = equal("categories", filter.category());
        if (category != null) {
            conditions.add(category);
        }
        for (Facet facet : facets) {
            if (!facet.equals(without) && facet.condition() != null) {
                conditions.add(facet.condition());
            }
        }
        if (!conditions.isEmpty()) {
            search.setFilterBy(String.join(" && ", conditions));
        }
        return search;
    }

    private static void facetBy(MultiSearchCollectionParameters search, List<Facet> facets) {
        if (!facets.isEmpty()) {
            search.setFacetBy(String.join(",", facets.stream().map(Facet::field).toList()));
            search.setMaxFacetValues(ALL_VALUES);
        }
    }

    private static void requireAnswered(MultiSearchResultItem result) {
        if (result.getError() != null) {
            throw new SearchIndexException("Typesense refused a search of the software catalog: " + result.getError());
        }
    }

    private record Value(String value, long count) {}

    private static <T> List<T> values(Map<String, List<FacetCounts>> counts, String field, Function<Value, T> read) {
        List<FacetCounts> of = counts.get(field);
        return of == null
                ? List.of()
                : of.stream()
                        .filter(c -> field.equals(c.getFieldName()))
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
                releaseDate == null ? null : String.valueOf(releaseDate),
                doc.get("screenshots") instanceof List<?> screenshots
                        ? screenshots.stream()
                                .map(id -> UUID.fromString(String.valueOf(id)))
                                .toList()
                        : List.of(),
                doc.get("made_by") instanceof List<?> madeBy
                        ? madeBy.stream().map(String::valueOf).toList()
                        : List.of());
    }
}

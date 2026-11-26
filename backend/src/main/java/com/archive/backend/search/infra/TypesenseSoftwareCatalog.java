package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    /**
     * A filter of the catalog: the field it is counted by, and its condition while a value is chosen. The hardware is
     * one field, filtered by group: a filter of each group chosen, and one counting the groups not chosen.
     */
    private record Facet(
            String field,
            @Nullable String condition,
            @Nullable String group) {

        Facet(String field, @Nullable String condition) {
            this(field, condition, null);
        }

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
        String hardware = TypesenseSearchIndex.HARDWARE_FIELD;
        facets.add(new Facet(hardware, null));
        filter.hardware().forEach((group, codes) -> {
            String condition = anyOf(
                    hardware,
                    codes.stream()
                            .map(code -> TypesenseSearchIndex.hardwareValue(group, code))
                            .toList());
            if (condition != null) {
                facets.add(new Facet(hardware, condition, group));
            }
        });
        return facets;
    }

    @Override
    public SoftwareCatalogPage find(SoftwareCatalogFilter filter, Pageable pageable) {
        List<Facet> facets = facets(filter);
        List<Facet> chosen = facets.stream().filter(Facet::chosen).toList();
        List<Facet> open = facets.stream().filter(facet -> !facet.chosen()).toList();

        List<MultiSearchCollectionParameters> searches = new ArrayList<>();
        MultiSearchCollectionParameters page = search(filter, facets, null);
        page.setSortBy(
                pageable.getSort().getOrderFor(SoftwareCatalog.RATING) == null
                        ? "title_sort:asc"
                        : "rating:desc,title_sort:asc");
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
        Map<Facet, List<FacetCounts>> counts = new HashMap<>();
        open.forEach(facet -> counts.put(facet, items.getFacetCounts()));
        for (int i = 0; i < chosen.size(); i++) {
            counts.put(chosen.get(i), results.get(i + 1).getFacetCounts());
        }

        // Each chosen group is counted by its own search, every other group by the page.
        Set<String> chosenGroups =
                chosen.stream().map(Facet::group).filter(Objects::nonNull).collect(Collectors.toSet());
        List<CodeCount> hardware = new ArrayList<>();
        facets.stream()
                .filter(facet -> facet.field().equals(TypesenseSearchIndex.HARDWARE_FIELD))
                .forEach(facet -> values(counts, facet, value -> value).stream()
                        .filter(value -> {
                            String group = groupOf(value);
                            String own = facet.group();
                            return own == null ? !chosenGroups.contains(group) : own.equals(group);
                        })
                        .map(value -> new CodeCount(codeOf(value), value.count()))
                        .forEach(hardware::add));
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
                                facets,
                                "year",
                                value -> new YearCount(Integer.parseInt(value.value()), value.count())),
                        values(counts, facets, "countries", TypesenseSoftwareCatalog::id),
                        values(counts, facets, "cities", TypesenseSoftwareCatalog::id),
                        values(counts, facets, "platform", TypesenseSoftwareCatalog::code),
                        hardware));
    }

    private static IdCount id(Value value) {
        return new IdCount(UUID.fromString(value.value()), value.count());
    }

    private static CodeCount code(Value value) {
        return new CodeCount(value.value(), value.count());
    }

    /** The group of an item of hardware the index counts as {@code group:code}. */
    private static String groupOf(Value value) {
        return value.value().substring(0, value.value().indexOf(':'));
    }

    /** The code of an item of hardware the index counts as {@code group:code}. */
    private static String codeOf(Value value) {
        return value.value().substring(value.value().indexOf(':') + 1);
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

    /** The values of the one filter of the field: every field but the hardware has a single one. */
    private static <T> List<T> values(
            Map<Facet, List<FacetCounts>> counts, List<Facet> facets, String field, Function<Value, T> read) {
        return facets.stream()
                .filter(facet -> facet.field().equals(field))
                .findFirst()
                .map(facet -> values(counts, facet, read))
                .orElse(List.of());
    }

    private static <T> List<T> values(Map<Facet, List<FacetCounts>> counts, Facet facet, Function<Value, T> read) {
        List<FacetCounts> of = counts.get(facet);
        return of == null
                ? List.of()
                : of.stream()
                        .filter(c -> facet.field().equals(c.getFieldName()))
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
                        : List.of(),
                doc.get("rating") instanceof Number rating ? rating.doubleValue() : 0,
                doc.get("votes_amount") instanceof Number votes ? votes.intValue() : 0);
    }
}

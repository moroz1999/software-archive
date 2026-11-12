package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

import com.archive.backend.search.read.QuickAuthor;
import com.archive.backend.search.read.QuickGroup;
import com.archive.backend.search.read.QuickSearch;
import com.archive.backend.search.read.QuickSearchResult;
import com.archive.backend.search.read.QuickSection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.typesense.api.Client;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchResultItem;
import org.typesense.model.MultiSearchSearchesParameter;

/**
 * The quick search in one {@code multi_search} over the collections of software, people and groups, each giving its
 * first matches and the count of all of them (docs/features/search.md#reads).
 */
@Repository
@RequiredArgsConstructor
public class TypesenseQuickSearch implements QuickSearch {

    private final Client client;

    @Override
    public QuickSearchResult find(String keyword, @Nullable String platform, int limit) {
        List<MultiSearchCollectionParameters> searches = List.of(
                search("software", "title,alternative_titles", "title_sort", "platform", keyword, platform, limit),
                search("authors", "name,labels,real_name", "name_sort", "platforms", keyword, platform, limit),
                search("groups", "name,labels,group_name", "name_sort", "platforms", keyword, platform, limit));
        List<MultiSearchResultItem> results = call(
                "quick search",
                () -> client.multiSearch
                        .perform(new MultiSearchSearchesParameter().searches(searches), Map.of())
                        .getResults());
        return new QuickSearchResult(
                section(results.get(0), TypesenseSoftwareCatalog::item),
                section(results.get(1), TypesenseQuickSearch::author),
                section(results.get(2), TypesenseQuickSearch::group));
    }

    /** The best matches first; equal ones in name order. */
    private static MultiSearchCollectionParameters search(
            String collection,
            String queryBy,
            String sortField,
            String platformField,
            String keyword,
            @Nullable String platform,
            int limit) {
        MultiSearchCollectionParameters search = new MultiSearchCollectionParameters().collection(collection);
        search.setQ(keyword);
        search.setQueryBy(queryBy);
        search.setSortBy("_text_match:desc," + sortField + ":asc");
        search.setPerPage(limit);
        if (platform != null) {
            search.setFilterBy(platformField + ":=`" + platform + "`");
        }
        return search;
    }

    private static <T> QuickSection<T> section(MultiSearchResultItem result, Function<Map<String, Object>, T> read) {
        if (result.getError() != null) {
            throw new SearchIndexException("Typesense refused a quick search: " + result.getError());
        }
        return new QuickSection<>(
                result.getHits().stream()
                        .map(hit -> read.apply(hit.getDocument()))
                        .toList(),
                Objects.requireNonNullElse(result.getFound(), 0));
    }

    private static QuickAuthor author(Map<String, Object> doc) {
        String name = String.valueOf(doc.get("name"));
        Object realName = doc.get("real_name");
        return new QuickAuthor(
                UUID.fromString(String.valueOf(doc.get("id"))),
                name,
                realName == null || name.equals(realName) ? null : String.valueOf(realName));
    }

    private static QuickGroup group(Map<String, Object> doc) {
        return new QuickGroup(UUID.fromString(String.valueOf(doc.get("id"))), String.valueOf(doc.get("name")));
    }
}

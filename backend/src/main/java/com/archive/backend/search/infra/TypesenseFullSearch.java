package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

import com.archive.backend.search.read.FoundSet;
import com.archive.backend.search.read.FoundType;
import com.archive.backend.search.read.FullSearch;
import com.archive.backend.search.read.FullSearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.typesense.api.Client;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchResultItem;
import org.typesense.model.MultiSearchSearchesParameter;

/**
 * The full search in two {@code multi_search}: the count of every kind, then the part of each kind that falls on the
 * page (docs/features/search.md#reads).
 */
@Repository
@RequiredArgsConstructor
public class TypesenseFullSearch implements FullSearch {

    private final Client client;

    /** The part of a kind on the page: its matches from {@code offset}, {@code limit} of them. */
    private record Window(FoundType kind, long total, int offset, int limit) {}

    @Override
    public FullSearchResult find(
            String keyword, @Nullable String platform, Set<FoundType> kinds, int number, int size) {
        List<FoundType> asked =
                Arrays.stream(FoundType.values()).filter(kinds::contains).toList();
        List<MultiSearchResultItem> counts = perform(asked.stream()
                .map(kind -> section(kind).search(keyword, platform, 0, 0))
                .toList());

        long total = 0;
        long from = (long) number * size;
        long to = from + size;
        List<Window> windows = new ArrayList<>();
        for (int i = 0; i < asked.size(); i++) {
            long found = Objects.requireNonNullElse(counts.get(i).getFound(), 0);
            long start = Math.max(from, total);
            long end = Math.min(to, total + found);
            if (start < end) {
                windows.add(new Window(asked.get(i), found, (int) (start - total), (int) (end - start)));
            }
            total += found;
        }
        if (windows.isEmpty()) {
            return new FullSearchResult(number, size, total, List.of());
        }

        List<MultiSearchResultItem> pages = perform(windows.stream()
                .map(w -> section(w.kind()).window(keyword, platform, w.offset(), w.limit()))
                .toList());
        List<FoundSet> sets = new ArrayList<>();
        for (int i = 0; i < windows.size(); i++) {
            sets.add(set(windows.get(i), documents(pages.get(i))));
        }
        return new FullSearchResult(number, size, total, sets);
    }

    private List<MultiSearchResultItem> perform(List<MultiSearchCollectionParameters> searches) {
        if (searches.isEmpty()) {
            return List.of();
        }
        List<MultiSearchResultItem> results = call(
                "full search",
                () -> client.multiSearch
                        .perform(new MultiSearchSearchesParameter().searches(searches), Map.of())
                        .getResults());
        results.forEach(TypesenseSection::answered);
        return results;
    }

    private static List<Map<String, Object>> documents(MultiSearchResultItem result) {
        return result.getHits().stream().map(hit -> hit.getDocument()).toList();
    }

    private static FoundSet set(Window window, List<Map<String, Object>> documents) {
        return switch (window.kind()) {
            case AUTHOR ->
                new FoundSet.Authors(
                        window.total(),
                        documents.stream().map(TypesenseQuickSearch::author).toList());
            case GROUP ->
                new FoundSet.Groups(
                        window.total(),
                        documents.stream().map(TypesenseQuickSearch::group).toList());
            case SOFTWARE ->
                new FoundSet.Software(
                        window.total(),
                        documents.stream().map(TypesenseSoftwareCatalog::item).toList());
        };
    }

    private static TypesenseSection section(FoundType kind) {
        return switch (kind) {
            case AUTHOR -> TypesenseSection.AUTHORS;
            case GROUP -> TypesenseSection.GROUPS;
            case SOFTWARE -> TypesenseSection.SOFTWARE;
        };
    }
}

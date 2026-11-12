package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.read.FullSearch;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.typesense.api.Client;
import org.typesense.model.MultiSearchResultItem;
import org.typesense.model.MultiSearchSearchesParameter;

/** The full search of people and of groups, a page of one collection at a time (docs/features/search.md#reads). */
@Repository
@RequiredArgsConstructor
public class TypesenseFullSearch implements FullSearch {

    private final Client client;

    @Override
    public Page<FoundAuthor> authors(String keyword, @Nullable String platform, Pageable pageable) {
        return page(TypesenseSection.AUTHORS, keyword, platform, pageable, TypesenseQuickSearch::author);
    }

    @Override
    public Page<FoundGroup> groups(String keyword, @Nullable String platform, Pageable pageable) {
        return page(TypesenseSection.GROUPS, keyword, platform, pageable, TypesenseQuickSearch::group);
    }

    private <T> Page<T> page(
            TypesenseSection section,
            String keyword,
            @Nullable String platform,
            Pageable pageable,
            Function<Map<String, Object>, T> read) {
        MultiSearchSearchesParameter searches = new MultiSearchSearchesParameter()
                .searches(List.of(section.search(keyword, platform, pageable.getPageNumber(), pageable.getPageSize())));
        MultiSearchResultItem result = TypesenseSection.answered(call(
                        "full search",
                        () -> client.multiSearch.perform(searches, Map.of()).getResults())
                .getFirst());
        return new PageImpl<>(
                result.getHits().stream()
                        .map(hit -> read.apply(hit.getDocument()))
                        .toList(),
                pageable,
                Objects.requireNonNullElse(result.getFound(), 0));
    }
}

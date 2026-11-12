package com.archive.backend.search.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.FoundKind;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundSet;
import com.archive.backend.search.read.FoundType;
import com.archive.backend.search.read.FullSearch;
import com.archive.backend.search.read.FullSearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

/** The full search (docs/domain/search.md#full-search). */
class FullSearchHandlerTest {

    private static final FullSearchResult FOUND = new FullSearchResult(
            1, 50, 51, List.of(new FoundSet.Authors(51, List.of(new FoundAuthor(UUID.randomUUID(), "Zed", null)))));

    private final List<String> asked = new ArrayList<>();

    private final FullSearch search =
            (String keyword, @Nullable String platform, Set<FoundType> kinds, int number, int size) -> {
                asked.add(keyword + "|" + platform + "|" + new TreeSet<>(kinds) + "|" + number + "|" + size);
                return FOUND;
            };

    private final FullSearchHandler handler = new FullSearchHandler(search);

    @Test
    void asksForAPageOfFiftyOfTheChosenKindsOfThePlatform() {
        FullSearchResult result = handler.handle(new FullSearchQuery(
                new CatalogKeyword("zed"),
                new PlatformCode("vector"),
                Set.of(FoundKind.GROUP, FoundKind.AUTHOR),
                PageRequest.of(1, 20)));

        assertThat(result).isEqualTo(FOUND);
        assertThat(asked).containsExactly("zed|vector|[AUTHOR, GROUP]|1|50");
    }

    @Test
    void asksForEveryKindWhenNoneIsChosen() {
        handler.handle(new FullSearchQuery(new CatalogKeyword("zed"), null, Set.of(), PageRequest.of(0, 20)));

        assertThat(asked).containsExactly("zed|null|[AUTHOR, GROUP, SOFTWARE]|0|50");
    }

    @Test
    void findsNothingWithoutAKeywordAndDoesNotSearch() {
        assertThat(handler.handle(new FullSearchQuery(null, null, Set.of(), PageRequest.of(2, 20))))
                .isEqualTo(FullSearchResult.empty(2, 50));
        assertThat(asked).isEmpty();
    }
}

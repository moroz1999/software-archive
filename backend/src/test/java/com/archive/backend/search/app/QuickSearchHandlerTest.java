package com.archive.backend.search.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.QuickGroup;
import com.archive.backend.search.read.QuickSearch;
import com.archive.backend.search.read.QuickSearchResult;
import com.archive.backend.search.read.QuickSection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** The quick search (docs/domain/search.md#quick-search). */
class QuickSearchHandlerTest {

    private static final QuickSearchResult FOUND = new QuickSearchResult(
            QuickSection.empty(),
            QuickSection.empty(),
            new QuickSection<>(List.of(new QuickGroup(UUID.randomUUID(), "ZS")), 1));

    private final List<String> asked = new ArrayList<>();

    private final QuickSearch search = (String keyword, @Nullable String platform, int limit) -> {
        asked.add(keyword + "|" + platform + "|" + limit);
        return FOUND;
    };

    private final QuickSearchHandler handler = new QuickSearchHandler(search);

    @Test
    void asksForTheFirstFiveMatchesOfEverySectionOfThePlatform() {
        QuickSearchResult result =
                handler.handle(new QuickSearchQuery(new CatalogKeyword("zed"), new PlatformCode("vector")));

        assertThat(result).isEqualTo(FOUND);
        assertThat(asked).containsExactly("zed|vector|5");
    }

    @Test
    void asksOfEveryPlatformWithoutOne() {
        handler.handle(new QuickSearchQuery(new CatalogKeyword("zed"), null));

        assertThat(asked).containsExactly("zed|null|5");
    }

    @Test
    void findsNothingWithoutAKeywordAndDoesNotSearch() {
        assertThat(handler.handle(new QuickSearchQuery(null, null))).isEqualTo(QuickSearchResult.empty());
        assertThat(asked).isEmpty();
    }
}

package com.archive.backend.search.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.read.FullSearch;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** The full search (docs/domain/search.md#full-search). */
class FullSearchHandlerTest {

    private static final Pageable SECOND = PageRequest.of(1, 50);

    private static final Page<FoundAuthor> AUTHORS =
            new PageImpl<>(List.of(new FoundAuthor(UUID.randomUUID(), "Zed", null)), SECOND, 51);
    private static final Page<FoundGroup> GROUPS =
            new PageImpl<>(List.of(new FoundGroup(UUID.randomUUID(), "ZS")), SECOND, 51);

    private final List<String> asked = new ArrayList<>();

    private final FullSearch search = new FullSearch() {
        @Override
        public Page<FoundAuthor> authors(String keyword, @Nullable String platform, Pageable pageable) {
            asked.add("authors|" + keyword + "|" + platform + "|" + pageable.getPageNumber() + "|"
                    + pageable.getPageSize());
            return AUTHORS;
        }

        @Override
        public Page<FoundGroup> groups(String keyword, @Nullable String platform, Pageable pageable) {
            asked.add("groups|" + keyword + "|" + platform + "|" + pageable.getPageNumber() + "|"
                    + pageable.getPageSize());
            return GROUPS;
        }
    };

    private final FullSearchHandler handler = new FullSearchHandler(search);

    @Test
    void asksForThePageOfPeopleOfThePlatform() {
        Page<FoundAuthor> page =
                handler.authors(new FullSearchQuery(new CatalogKeyword("zed"), new PlatformCode("vector"), SECOND));

        assertThat(page).isEqualTo(AUTHORS);
        assertThat(asked).containsExactly("authors|zed|vector|1|50");
    }

    @Test
    void asksForThePageOfGroupsOfEveryPlatformWithoutOne() {
        Page<FoundGroup> page = handler.groups(new FullSearchQuery(new CatalogKeyword("zed"), null, SECOND));

        assertThat(page).isEqualTo(GROUPS);
        assertThat(asked).containsExactly("groups|zed|null|1|50");
    }

    @Test
    void findsNothingWithoutAKeywordAndDoesNotSearch() {
        FullSearchQuery query = new FullSearchQuery(null, null, SECOND);

        assertThat(handler.authors(query)).isEmpty();
        assertThat(handler.groups(query).getTotalElements()).isZero();
        assertThat(asked).isEmpty();
    }
}

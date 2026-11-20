package com.archive.backend.category.infra;

import static com.archive.backend.category.infra.SeededTree.DEMOSCENE;
import static com.archive.backend.category.infra.SeededTree.GAMES;
import static com.archive.backend.category.infra.SeededTree.INTRO;
import static com.archive.backend.category.infra.SeededTree.INTRO_4K;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryLine;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Looks categories up in the tree the schema brings. */
@IntegrationTest
@Transactional
class JdbcCategoryCatalogIT {

    private static final CategoryId GONE = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000009"));

    @Autowired
    private JdbcCategoryCatalog catalog;

    @Test
    void readsTheLinesOfCategoriesAtAnyDepthUpToTheTop() {
        assertThat(catalog.linesOf(Set.of(new CategoryId(GAMES), new CategoryId(INTRO_4K))))
                .containsExactlyInAnyOrder(
                        new CategoryLine(new CategoryId(GAMES), List.of()),
                        new CategoryLine(
                                new CategoryId(INTRO_4K), List.of(new CategoryId(INTRO), new CategoryId(DEMOSCENE))));
    }

    @Test
    void readsNoLineOfACategoryOutsideTheArchive() {
        assertThat(catalog.linesOf(Set.of(new CategoryId(GAMES), GONE)))
                .containsExactly(new CategoryLine(new CategoryId(GAMES), List.of()));
    }

    @Test
    void readsNoLinesOfNoCategories() {
        assertThat(catalog.linesOf(Set.of())).isEmpty();
    }
}

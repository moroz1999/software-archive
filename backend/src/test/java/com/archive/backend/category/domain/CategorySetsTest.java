package com.archive.backend.category.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategorySetsTest {

    private static final CategoryId GAMES = id(1);
    private static final CategoryId ARCADE = id(2);
    private static final CategoryId DEMOSCENE = id(3);
    private static final CategoryId GONE = id(9);

    /** Games → Arcade and Demoscene: the tree of the archive. */
    private static final Map<CategoryId, CategoryLine> LINES = Map.of(
            GAMES, new CategoryLine(GAMES, List.of()),
            ARCADE, new CategoryLine(ARCADE, List.of(GAMES)),
            DEMOSCENE, new CategoryLine(DEMOSCENE, List.of()));

    private final CategorySets sets = new CategorySets(
            ids -> ids.stream().filter(LINES::containsKey).map(LINES::get).toList());

    private static CategoryId id(int n) {
        return new CategoryId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }

    @Test
    void buildsTheCategoriesOfTheArchive() {
        assertThat(sets.of(Set.of(ARCADE, DEMOSCENE)).ids()).containsExactlyInAnyOrder(ARCADE, DEMOSCENE);
    }

    @Test
    void buildsTheCategoriesByTheirLinesInTheArchive() {
        assertThat(sets.of(Set.of(GAMES, ARCADE)).ids()).containsExactly(ARCADE);
    }

    @Test
    void buildsNoCategories() {
        assertThat(sets.of(Set.of())).isEqualTo(CategorySet.EMPTY);
    }

    @Test
    void refusesACategoryNotInTheArchive() {
        assertThatThrownBy(() -> sets.of(Set.of(GAMES, GONE)))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("category.unknown");
                    assertThat(e.field()).isEqualTo("categories");
                });
    }
}

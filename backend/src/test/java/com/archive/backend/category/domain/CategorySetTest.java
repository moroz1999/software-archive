package com.archive.backend.category.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Never a category together with one above it (docs/domain/categories.md). */
class CategorySetTest {

    private static final CategoryId GAMES = id(1);
    private static final CategoryId ACTION = id(2);
    private static final CategoryId SHOOTERS = id(3);
    private static final CategoryId PUZZLE = id(4);
    private static final CategoryId DEMOSCENE = id(5);

    // Games → Action → Shooters, Games → Puzzle, and Demoscene beside Games.
    private static final CategoryLine GAMES_LINE = new CategoryLine(GAMES, List.of());
    private static final CategoryLine ACTION_LINE = new CategoryLine(ACTION, List.of(GAMES));
    private static final CategoryLine SHOOTERS_LINE = new CategoryLine(SHOOTERS, List.of(ACTION, GAMES));
    private static final CategoryLine PUZZLE_LINE = new CategoryLine(PUZZLE, List.of(GAMES));
    private static final CategoryLine DEMOSCENE_LINE = new CategoryLine(DEMOSCENE, List.of());

    private static CategoryId id(int n) {
        return new CategoryId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }

    @Test
    void keepsCategoriesOfDifferentBranches() {
        assertThat(new CategorySet(List.of(SHOOTERS_LINE, PUZZLE_LINE, DEMOSCENE_LINE)).ids())
                .containsExactlyInAnyOrder(SHOOTERS, PUZZLE, DEMOSCENE);
    }

    @Test
    void leavesOutTheParentOfAnotherOfItsCategories() {
        assertThat(new CategorySet(List.of(GAMES_LINE, ACTION_LINE)).ids()).containsExactly(ACTION);
    }

    @Test
    void leavesOutEveryCategoryAboveAnotherOfItsAtAnyDepth() {
        assertThat(new CategorySet(List.of(GAMES_LINE, SHOOTERS_LINE, ACTION_LINE, DEMOSCENE_LINE)).ids())
                .containsExactlyInAnyOrder(SHOOTERS, DEMOSCENE);
    }

    @Test
    void keepsAParentAboveNoneOfItsOtherCategories() {
        assertThat(new CategorySet(List.of(ACTION_LINE, PUZZLE_LINE)).ids()).containsExactlyInAnyOrder(ACTION, PUZZLE);
    }

    @Test
    void holdsTheRuleForCategoriesReadBackFromStorage() {
        assertThat(CategorySet.restore(List.of(GAMES_LINE, SHOOTERS_LINE)).ids())
                .containsExactly(SHOOTERS);
    }

    @Test
    void isEmptyWithoutCategories() {
        assertThat(new CategorySet(List.of()).ids()).isEmpty();
        assertThat(CategorySet.EMPTY.ids()).isEmpty();
    }
}

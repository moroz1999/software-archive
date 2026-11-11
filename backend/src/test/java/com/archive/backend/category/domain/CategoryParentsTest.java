package com.archive.backend.category.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryParentsTest {

    private static final CategoryId GAMES = id(1);
    private static final CategoryId ACTION = id(2);
    private static final CategoryId SHOOTERS = id(3);
    private static final CategoryId DEMOSCENE = id(4);

    /** Games → Action → Shooters, and Demoscene beside them. */
    private static final Map<CategoryId, List<CategoryId>> LINES = Map.of(
            GAMES, List.of(GAMES),
            ACTION, List.of(ACTION, GAMES),
            SHOOTERS, List.of(SHOOTERS, ACTION, GAMES),
            DEMOSCENE, List.of(DEMOSCENE));

    private final CategoryParents parents = new CategoryParents(category -> Optional.ofNullable(LINES.get(category)));

    private static CategoryId id(int n) {
        return new CategoryId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }

    @Test
    void buildsAParentOfTheTreeForTheCategory() {
        CategoryParent parent = parents.of(SHOOTERS, DEMOSCENE);

        assertThat(parent.id()).isEqualTo(DEMOSCENE);
        assertThat(parent.category()).isEqualTo(SHOOTERS);
    }

    @Test
    void putsANewCategoryUnderAnyCategoryOfTheTree() {
        assertThat(parents.of(id(9), SHOOTERS).id()).isEqualTo(SHOOTERS);
    }

    @Test
    void movesACategoryUpItsOwnLine() {
        assertThat(parents.of(SHOOTERS, GAMES).id()).isEqualTo(GAMES);
    }

    @Test
    void refusesAParentOutsideTheArchive() {
        assertThatThrownBy(() -> parents.of(ACTION, id(9))).isInstanceOfSatisfying(RuleViolationException.class, e -> {
            assertThat(e.code()).isEqualTo("category.parent-unknown");
            assertThat(e.field()).isEqualTo("parentId");
        });
    }

    @Test
    void refusesToPutACategoryUnderItself() {
        assertThatThrownBy(() -> parents.of(ACTION, ACTION)).isInstanceOfSatisfying(RuleViolationException.class, e -> {
            assertThat(e.code()).isEqualTo("category.parent-inside");
            assertThat(e.field()).isEqualTo("parentId");
        });
    }

    @Test
    void refusesToPutACategoryUnderItsOwnSubcategory() {
        assertThatThrownBy(() -> parents.of(GAMES, SHOOTERS))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("category.parent-inside"));
    }

    @Test
    void restoresTheParentAsStored() {
        assertThat(CategoryParent.restore(SHOOTERS, DEMOSCENE)).isEqualTo(parents.of(SHOOTERS, DEMOSCENE));
    }
}

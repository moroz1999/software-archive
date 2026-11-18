package com.archive.backend.category.domain;

import static com.archive.backend.category.domain.CategoryTitleFixtures.titles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryTest {

    private static final CategoryId ACTION = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final CategoryId GAMES = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final CategoryId DEMOSCENE = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000004"));

    @Test
    void editsTheTitleAndTheParent() {
        Category action = Category.create(ACTION, titles("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(titles("Action games"), CategoryParent.restore(ACTION, DEMOSCENE));

        assertThat(action.getTitles()).isEqualTo(titles("Action games"));
        assertThat(action.getParent()).isEqualTo(CategoryParent.restore(ACTION, DEMOSCENE));
    }

    @Test
    void goesToTheTopOfTheTreeWithoutAParent() {
        Category action = Category.create(ACTION, titles("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(titles("Action"), null);

        assertThat(action.getParent()).isNull();
    }

    @Test
    void takesOnlyAParentCheckedForItself() {
        CategoryParent ofGames = CategoryParent.restore(GAMES, DEMOSCENE);

        assertThatThrownBy(() -> Category.create(ACTION, titles("Action"), ofGames))
                .isInstanceOf(IllegalArgumentException.class);
        Category action = Category.create(ACTION, titles("Action"), null);
        assertThatThrownBy(() -> action.edit(titles("Action"), ofGames)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void followsTheTitleRules() {
        assertThatThrownBy(() -> new CategoryTitle(" "))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.required"));
        assertThatThrownBy(() -> new CategoryTitle("a".repeat(256)))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.too-long"));
    }

    @Test
    void tellsItMovedUnderAnotherParent() {
        Category action = Category.create(ACTION, titles("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(titles("Action"), CategoryParent.restore(ACTION, DEMOSCENE));

        assertThat(action.pullEvents()).containsExactly(new CategoryMoved(ACTION));
        assertThat(action.pullEvents()).isEmpty();
    }

    @Test
    void tellsItMovedToTheTopAndFromIt() {
        Category action = Category.create(ACTION, titles("Action"), CategoryParent.restore(ACTION, GAMES));
        action.edit(titles("Action"), null);

        assertThat(action.pullEvents()).containsExactly(new CategoryMoved(ACTION));

        action.edit(titles("Action"), CategoryParent.restore(ACTION, GAMES));

        assertThat(action.pullEvents()).containsExactly(new CategoryMoved(ACTION));
    }

    @Test
    void tellsNothingWhenItStaysUnderItsParent() {
        Category action = Category.create(ACTION, titles("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(titles("Action games"), CategoryParent.restore(ACTION, GAMES));

        assertThat(action.pullEvents()).isEmpty();
    }

    @Test
    void tellsNothingWhenCreated() {
        assertThat(Category.create(ACTION, titles("Action"), null).pullEvents()).isEmpty();
    }
}

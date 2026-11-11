package com.archive.backend.category.domain;

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
        Category action = Category.create(ACTION, new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(new CategoryTitle("Action games"), CategoryParent.restore(ACTION, DEMOSCENE));

        assertThat(action.getTitle()).isEqualTo(new CategoryTitle("Action games"));
        assertThat(action.getParent()).isEqualTo(CategoryParent.restore(ACTION, DEMOSCENE));
    }

    @Test
    void goesToTheTopOfTheTreeWithoutAParent() {
        Category action = Category.create(ACTION, new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(new CategoryTitle("Action"), null);

        assertThat(action.getParent()).isNull();
    }

    @Test
    void takesOnlyAParentCheckedForItself() {
        CategoryParent ofGames = CategoryParent.restore(GAMES, DEMOSCENE);

        assertThatThrownBy(() -> Category.create(ACTION, new CategoryTitle("Action"), ofGames))
                .isInstanceOf(IllegalArgumentException.class);
        Category action = Category.create(ACTION, new CategoryTitle("Action"), null);
        assertThatThrownBy(() -> action.edit(new CategoryTitle("Action"), ofGames))
                .isInstanceOf(IllegalArgumentException.class);
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
        Category action = Category.create(ACTION, new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(new CategoryTitle("Action"), CategoryParent.restore(ACTION, DEMOSCENE));

        assertThat(action.pullEvents()).containsExactly(new CategoryMoved(ACTION));
        assertThat(action.pullEvents()).isEmpty();
    }

    @Test
    void tellsItMovedToTheTopAndFromIt() {
        Category action = Category.create(ACTION, new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES));
        action.edit(new CategoryTitle("Action"), null);

        assertThat(action.pullEvents()).containsExactly(new CategoryMoved(ACTION));

        action.edit(new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES));

        assertThat(action.pullEvents()).containsExactly(new CategoryMoved(ACTION));
    }

    @Test
    void tellsNothingWhenItStaysUnderItsParent() {
        Category action = Category.create(ACTION, new CategoryTitle("Action"), CategoryParent.restore(ACTION, GAMES));

        action.edit(new CategoryTitle("Action games"), CategoryParent.restore(ACTION, GAMES));

        assertThat(action.pullEvents()).isEmpty();
    }

    @Test
    void tellsNothingWhenCreated() {
        assertThat(Category.create(ACTION, new CategoryTitle("Action"), null).pullEvents())
                .isEmpty();
    }
}

package com.archive.backend.category.app;

import static com.archive.backend.category.app.CategoryFixtures.ACTION;
import static com.archive.backend.category.app.CategoryFixtures.DEMOSCENE;
import static com.archive.backend.category.app.CategoryFixtures.GAMES;
import static com.archive.backend.category.app.CategoryFixtures.SHOOTERS;
import static com.archive.backend.category.app.CategoryFixtures.id;
import static com.archive.backend.category.app.CategoryFixtures.tree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryParents;
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateCategoryHandlerTest {

    private final InMemoryCategoryRepository repository = tree();
    private final List<Object> published = new ArrayList<>();
    private final UpdateCategoryHandler handler =
            new UpdateCategoryHandler(repository, new CategoryParents(repository), published::add);

    @Test
    void storesTheNewTitleAndParent() {
        handler.handle(new UpdateCategoryCommand(ACTION, new CategoryTitle("Action games"), DEMOSCENE));

        assertThat(repository.findById(ACTION)).hasValueSatisfying(category -> {
            assertThat(category.getTitle()).isEqualTo(new CategoryTitle("Action games"));
            assertThat(category.getParent()).isEqualTo(CategoryParent.restore(ACTION, DEMOSCENE));
        });
    }

    @Test
    void movesTheCategoryToTheTopWithoutAParent() {
        handler.handle(new UpdateCategoryCommand(SHOOTERS, new CategoryTitle("Shooters"), null));

        assertThat(repository.findById(SHOOTERS))
                .hasValueSatisfying(category -> assertThat(category.getParent()).isNull());
    }

    @Test
    void failsWhenTheCategoryDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateCategoryCommand(id(9), new CategoryTitle("Misc"), null)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("category.not-found"));
    }

    @Test
    void refusesToPutTheCategoryUnderItsSubcategoryAndKeepsIt() {
        assertThatThrownBy(() -> handler.handle(new UpdateCategoryCommand(GAMES, new CategoryTitle("All"), SHOOTERS)))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("category.parent-inside"));
        assertThat(repository.findById(GAMES)).hasValueSatisfying(category -> {
            assertThat(category.getTitle()).isEqualTo(new CategoryTitle("Games"));
            assertThat(category.getParent()).isNull();
        });
    }

    @Test
    void refusesAParentOutsideTheArchive() {
        assertThatThrownBy(() -> handler.handle(new UpdateCategoryCommand(ACTION, new CategoryTitle("Action"), id(9))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("category.parent-unknown"));
    }

    @Test
    void tellsTheCategoryMoved() {
        handler.handle(new UpdateCategoryCommand(ACTION, new CategoryTitle("Action"), DEMOSCENE));

        assertThat(published).containsExactly(new CategoryMoved(ACTION));
    }

    @Test
    void tellsNothingWhenTheCategoryStaysUnderItsParent() {
        handler.handle(new UpdateCategoryCommand(ACTION, new CategoryTitle("Action games"), GAMES));

        assertThat(published).isEmpty();
    }
}

package com.archive.backend.category.app;

import static com.archive.backend.category.app.CategoryFixtures.ACTION;
import static com.archive.backend.category.app.CategoryFixtures.DEMOSCENE;
import static com.archive.backend.category.app.CategoryFixtures.GAMES;
import static com.archive.backend.category.app.CategoryFixtures.SHOOTERS;
import static com.archive.backend.category.app.CategoryFixtures.id;
import static com.archive.backend.category.app.CategoryFixtures.tree;
import static com.archive.backend.category.domain.CategoryTitleFixtures.titles;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryParents;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.domain.ChangeForbiddenException;
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
        handler.handle(new UpdateCategoryCommand(ADMIN, ACTION, titles("Action games"), DEMOSCENE));

        assertThat(repository.findById(ACTION)).hasValueSatisfying(category -> {
            assertThat(category.getTitles()).isEqualTo(titles("Action games"));
            assertThat(category.getParent()).isEqualTo(CategoryParent.restore(ACTION, DEMOSCENE));
        });
    }

    @Test
    void movesTheCategoryToTheTopWithoutAParent() {
        handler.handle(new UpdateCategoryCommand(ADMIN, SHOOTERS, titles("Shooters"), null));

        assertThat(repository.findById(SHOOTERS))
                .hasValueSatisfying(category -> assertThat(category.getParent()).isNull());
    }

    @Test
    void failsWhenTheCategoryDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new UpdateCategoryCommand(ADMIN, id(9), titles("Misc"), null)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("category.not-found"));
    }

    @Test
    void refusesToPutTheCategoryUnderItsSubcategoryAndKeepsIt() {
        assertThatThrownBy(() -> handler.handle(new UpdateCategoryCommand(ADMIN, GAMES, titles("All"), SHOOTERS)))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("category.parent-inside"));
        assertThat(repository.findById(GAMES)).hasValueSatisfying(category -> {
            assertThat(category.getTitles()).isEqualTo(titles("Games"));
            assertThat(category.getParent()).isNull();
        });
    }

    @Test
    void refusesAParentOutsideTheArchive() {
        assertThatThrownBy(() -> handler.handle(new UpdateCategoryCommand(ADMIN, ACTION, titles("Action"), id(9))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("category.parent-unknown"));
    }

    @Test
    void tellsTheCategoryMoved() {
        handler.handle(new UpdateCategoryCommand(ADMIN, ACTION, titles("Action"), DEMOSCENE));

        assertThat(published).containsExactly(new CategoryMoved(ACTION));
    }

    @Test
    void tellsNothingWhenTheCategoryStaysUnderItsParent() {
        handler.handle(new UpdateCategoryCommand(ADMIN, ACTION, titles("Action games"), GAMES));

        assertThat(published).isEmpty();
    }

    @Test
    void refusesAMemberAndKeepsTheCategory() {
        assertThatThrownBy(() ->
                        handler.handle(new UpdateCategoryCommand(MEMBER, ACTION, titles("Action games"), DEMOSCENE)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(ACTION))
                .hasValueSatisfying(category -> assertThat(category.getTitles()).isNotEqualTo(titles("Action games")));
    }
}

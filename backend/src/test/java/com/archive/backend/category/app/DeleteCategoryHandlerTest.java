package com.archive.backend.category.app;

import static com.archive.backend.category.app.CategoryFixtures.ACTION;
import static com.archive.backend.category.app.CategoryFixtures.DEMOSCENE;
import static com.archive.backend.category.app.CategoryFixtures.GAMES;
import static com.archive.backend.category.app.CategoryFixtures.SHOOTERS;
import static com.archive.backend.category.app.CategoryFixtures.id;
import static com.archive.backend.category.app.CategoryFixtures.tree;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.user.domain.ChangeForbiddenException;
import org.junit.jupiter.api.Test;

class DeleteCategoryHandlerTest {

    private final InMemoryCategoryRepository repository = tree();
    private final DeleteCategoryHandler handler = new DeleteCategoryHandler(repository, event -> {});

    @Test
    void removesTheCategoryWithItsSubtree() {
        handler.handle(new DeleteCategoryCommand(ADMIN, ACTION));

        assertThat(repository.findById(ACTION)).isEmpty();
        assertThat(repository.findById(SHOOTERS)).isEmpty();
        assertThat(repository.findById(GAMES)).isPresent();
        assertThat(repository.findById(DEMOSCENE)).isPresent();
    }

    @Test
    void failsWhenTheCategoryDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteCategoryCommand(ADMIN, id(9))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("category.not-found"));
    }

    @Test
    void refusesAMemberAndKeepsTheCategory() {
        assertThatThrownBy(() -> handler.handle(new DeleteCategoryCommand(MEMBER, ACTION)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(ACTION)).isPresent();
    }
}

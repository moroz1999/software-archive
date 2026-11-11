package com.archive.backend.category.app;

import static com.archive.backend.category.app.CategoryFixtures.ACTION;
import static com.archive.backend.category.app.CategoryFixtures.id;
import static com.archive.backend.category.app.CategoryFixtures.tree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryParents;
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.shared.domain.error.RuleViolationException;
import org.junit.jupiter.api.Test;

class CreateCategoryHandlerTest {

    private final InMemoryCategoryRepository repository = tree();
    private final CreateCategoryHandler handler =
            new CreateCategoryHandler(repository, new CategoryParents(repository));

    @Test
    void storesANewCategoryUnderItsParent() {
        CategoryId id = handler.handle(new CreateCategoryCommand(new CategoryTitle("Platformers"), ACTION));

        assertThat(repository.findById(id)).hasValueSatisfying(category -> {
            assertThat(category.getTitle()).isEqualTo(new CategoryTitle("Platformers"));
            assertThat(category.getParent()).isEqualTo(CategoryParent.restore(id, ACTION));
        });
    }

    @Test
    void storesANewCategoryAtTheTopWithoutAParent() {
        CategoryId id = handler.handle(new CreateCategoryCommand(new CategoryTitle("Series"), null));

        assertThat(repository.findById(id))
                .hasValueSatisfying(category -> assertThat(category.getParent()).isNull());
    }

    @Test
    void givesEveryCategoryItsOwnId() {
        CreateCategoryCommand command = new CreateCategoryCommand(new CategoryTitle("Misc"), null);

        assertThat(handler.handle(command)).isNotEqualTo(handler.handle(command));
    }

    @Test
    void refusesAParentOutsideTheArchiveAndStoresNothing() {
        int before = repository.count();

        assertThatThrownBy(() -> handler.handle(new CreateCategoryCommand(new CategoryTitle("Platformers"), id(9))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("category.parent-unknown"));
        assertThat(repository.count()).isEqualTo(before);
    }
}

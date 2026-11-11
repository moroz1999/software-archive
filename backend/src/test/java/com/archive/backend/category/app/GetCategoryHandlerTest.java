package com.archive.backend.category.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.shared.domain.error.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetCategoryHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final InMemoryCategoryReadRepository reads = new InMemoryCategoryReadRepository();
    private final GetCategoryHandler handler = new GetCategoryHandler(reads);

    @Test
    void returnsTheCategory() {
        CategoryDetails action = new CategoryDetails(ID, "Action", GAMES);
        reads.add(action);

        assertThat(handler.handle(new GetCategoryQuery(new CategoryId(ID)))).isEqualTo(action);
    }

    @Test
    void failsWhenTheCategoryDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new GetCategoryQuery(new CategoryId(ID))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("category.not-found"));
    }
}

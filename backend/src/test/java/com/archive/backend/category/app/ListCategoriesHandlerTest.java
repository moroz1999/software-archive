package com.archive.backend.category.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.read.CategoryNode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListCategoriesHandlerTest {

    private final InMemoryCategoryReadRepository reads = new InMemoryCategoryReadRepository();
    private final ListCategoriesHandler handler = new ListCategoriesHandler(reads);

    @Test
    void returnsTheWholeTree() {
        CategoryNode games = new CategoryNode(
                UUID.randomUUID(), "Games", List.of(new CategoryNode(UUID.randomUUID(), "Action", List.of())));
        CategoryNode demoscene = new CategoryNode(UUID.randomUUID(), "Demoscene", List.of());
        reads.add(games);
        reads.add(demoscene);

        assertThat(handler.handle(new ListCategoriesQuery())).containsExactly(games, demoscene);
    }
}

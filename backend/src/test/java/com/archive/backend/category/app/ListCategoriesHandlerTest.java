package com.archive.backend.category.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.shared.domain.Language;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListCategoriesHandlerTest {

    private final InMemoryCategoryReadRepository reads = new InMemoryCategoryReadRepository();
    private final ListCategoriesHandler handler = new ListCategoriesHandler(reads);

    @Test
    void returnsTheWholeTreeInTheLanguageAskedFor() {
        UUID gamesId = UUID.randomUUID();
        CategoryNode games =
                new CategoryNode(gamesId, "Игры", List.of(new CategoryNode(UUID.randomUUID(), "Экшен", List.of())));
        CategoryNode demoscene = new CategoryNode(UUID.randomUUID(), "Демосцена", List.of());
        reads.add("ru", games);
        reads.add("ru", demoscene);
        reads.add("en", new CategoryNode(gamesId, "Games", List.of()));

        assertThat(handler.handle(new ListCategoriesQuery(Language.RU))).containsExactly(games, demoscene);
    }
}

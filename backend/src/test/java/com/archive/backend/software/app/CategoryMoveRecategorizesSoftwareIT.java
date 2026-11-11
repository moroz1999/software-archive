package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.category.app.UpdateCategoryCommand;
import com.archive.backend.category.app.UpdateCategoryHandler;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryTitle;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Moving a category takes software out of every category the move put above another of its own
 * (docs/domain/categories.md), in the transaction of the move.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class CategoryMoveRecategorizesSoftwareIT {

    /** In Demoscene and in Shooters, of Games → Action. */
    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-00000000a001");
    /** In Strategy and in Shooters: Strategy is not above Action after the move. */
    private static final UUID TETRIS = UUID.fromString("00000000-0000-0000-0000-00000000a002");

    // Of the tree the schema brings.
    private static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final UUID ACTION = UUID.fromString("93c89b73-d81b-5b8f-9318-6a52895305b3");
    private static final UUID SHOOTERS = UUID.fromString("36d8df17-2b5b-518b-ac38-c3df4913be05");
    private static final UUID STRATEGY = UUID.fromString("623ec340-9a17-5999-9331-9a52e9fa6646");
    private static final UUID DEMOSCENE = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");

    @Autowired
    private UpdateCategoryHandler handler;

    @Autowired
    private JdbcTemplate jdbc;

    private void link(UUID software, UUID category) {
        jdbc.update("INSERT INTO software_category (software_id, category_id) VALUES (?, ?)", software, category);
    }

    private List<UUID> categoriesOf(UUID software) {
        return jdbc.queryForList(
                "SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, software);
    }

    @BeforeEach
    void insert() {
        jdbc.update(
                "INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector'), (?, 'Tetris', 'vector')",
                ELITE,
                TETRIS);
        link(ELITE, DEMOSCENE);
        link(ELITE, SHOOTERS);
        link(TETRIS, STRATEGY);
        link(TETRIS, SHOOTERS);
    }

    /** Commits for real, so the tree the schema brings is put back. */
    @AfterEach
    void putBack() {
        jdbc.update("DELETE FROM software WHERE id IN (?, ?)", ELITE, TETRIS);
        jdbc.update("UPDATE category SET parent_id = ? WHERE id = ?", GAMES, ACTION);
    }

    @Test
    void takesTheSoftwareOutOfACategoryTheMovePutAboveAnotherOfItsOwn() {
        handler.handle(new UpdateCategoryCommand(
                new CategoryId(ACTION), new CategoryTitle("Action"), new CategoryId(DEMOSCENE)));

        assertThat(categoriesOf(ELITE)).containsExactly(SHOOTERS);
        assertThat(categoriesOf(TETRIS)).containsExactlyInAnyOrder(STRATEGY, SHOOTERS);
    }
}

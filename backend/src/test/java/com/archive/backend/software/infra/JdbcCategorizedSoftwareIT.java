package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.software.domain.SoftwareId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** Finds the software a move of Games → Action under Demoscene puts on one branch twice. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcCategorizedSoftware.class})
class JdbcCategorizedSoftwareIT {

    /** In Demoscene and in Action. */
    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    /** In Action and Strategy, of Games: Strategy is not above Action after the move. */
    private static final UUID TETRIS = UUID.fromString("00000000-0000-0000-0000-000000000002");
    /** In Demoscene and in Shooters, under Action. */
    private static final UUID KNIGHT_LORE = UUID.fromString("00000000-0000-0000-0000-000000000003");
    /** In Games and in Shooters: Games is no longer above them after the move. */
    private static final UUID SAPPER = UUID.fromString("00000000-0000-0000-0000-000000000004");

    // Of the tree the schema brings.
    private static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final UUID ACTION = UUID.fromString("93c89b73-d81b-5b8f-9318-6a52895305b3");
    private static final UUID SHOOTERS = UUID.fromString("36d8df17-2b5b-518b-ac38-c3df4913be05");
    private static final UUID STRATEGY = UUID.fromString("623ec340-9a17-5999-9331-9a52e9fa6646");
    private static final UUID DEMOSCENE = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");

    @Autowired
    private JdbcCategorizedSoftware categorized;

    @Autowired
    private JdbcTemplate jdbc;

    private void link(UUID software, UUID category) {
        jdbc.update("INSERT INTO software_category (software_id, category_id) VALUES (?, ?)", software, category);
    }

    @BeforeEach
    void moveActionUnderDemoscene() {
        jdbc.update("""
                INSERT INTO software (id, title, platform_code) VALUES
                    (?, 'Elite', 'vector'), (?, 'Tetris', 'vector'), (?, 'Knight Lore', 'vector'), (?, 'Sapper', 'vector')
                """, ELITE, TETRIS, KNIGHT_LORE, SAPPER);
        link(ELITE, DEMOSCENE);
        link(ELITE, ACTION);
        link(TETRIS, ACTION);
        link(TETRIS, STRATEGY);
        link(KNIGHT_LORE, DEMOSCENE);
        link(KNIGHT_LORE, SHOOTERS);
        link(SAPPER, GAMES);
        link(SAPPER, SHOOTERS);
        jdbc.update("UPDATE category SET parent_id = ? WHERE id = ?", DEMOSCENE, ACTION);
    }

    @Test
    void findsTheSoftwareInTheMovedCategoryOrBelowItAndInACategoryNowAboveIt() {
        assertThat(categorized.inCategoriesAboveOthersAfterMoving(new CategoryId(ACTION)))
                .containsExactly(new SoftwareId(ELITE), new SoftwareId(KNIGHT_LORE));
    }

    @Test
    void findsNoSoftwareForAMoveThatBringsNothingTogether() {
        assertThat(categorized.inCategoriesAboveOthersAfterMoving(new CategoryId(STRATEGY)))
                .isEmpty();
    }
}

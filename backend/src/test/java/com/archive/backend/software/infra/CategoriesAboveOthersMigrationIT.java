package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The migration that takes software out of every category above another of its own, as it stood before the rule
 * (docs/domain/categories.md); run again here on links stored the old way.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CategoriesAboveOthersMigrationIT {

    private static final String MIGRATION = "db/migration/V9__categories_above_others.sql";

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TETRIS = UUID.fromString("00000000-0000-0000-0000-000000000002");
    // Of the tree the schema brings.
    private static final UUID GAMES = UUID.fromString("081bf3a1-782b-5a8b-8d12-3b155e0a58cb");
    private static final UUID ACTION = UUID.fromString("93c89b73-d81b-5b8f-9318-6a52895305b3");
    private static final UUID SHOOTERS = UUID.fromString("36d8df17-2b5b-518b-ac38-c3df4913be05");
    private static final UUID DEMOSCENE = UUID.fromString("c35b7f9b-44a8-5570-9406-9e6dcb30b607");

    @Autowired
    private JdbcTemplate jdbc;

    private void migrate() throws IOException {
        jdbc.execute(new ClassPathResource(MIGRATION).getContentAsString(StandardCharsets.UTF_8));
    }

    private void link(UUID software, UUID category) {
        jdbc.update("INSERT INTO software_category (software_id, category_id) VALUES (?, ?)", software, category);
    }

    private Iterable<UUID> categoriesOf(UUID software) {
        return jdbc.queryForList(
                "SELECT category_id FROM software_category WHERE software_id = ?", UUID.class, software);
    }

    @Test
    void takesSoftwareOutOfEveryCategoryAboveAnotherOfItsOwnOnly() throws IOException {
        jdbc.update(
                "INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector'), (?, 'Tetris', 'vector')",
                ELITE,
                TETRIS);
        link(ELITE, GAMES);
        link(ELITE, ACTION);
        link(ELITE, SHOOTERS);
        link(ELITE, DEMOSCENE);
        link(TETRIS, GAMES);

        migrate();

        assertThat(categoriesOf(ELITE)).containsExactlyInAnyOrder(SHOOTERS, DEMOSCENE);
        assertThat(categoriesOf(TETRIS)).containsExactly(GAMES);
    }
}

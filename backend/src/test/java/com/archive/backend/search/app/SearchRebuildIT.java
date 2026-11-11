package com.archive.backend.search.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.search.TypesenseProbe;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * The search collections are built anew from PostgreSQL and take over only when fully loaded
 * (docs/features/search.md#initial-load-and-rebuild).
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class SearchRebuildIT {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-00fe-000000000001");
    private static final UUID TETRIS = UUID.fromString("00000000-0000-0000-00fe-000000000002");

    @Autowired
    private RebuildSearchIndexHandler rebuild;

    @Autowired
    private ProcessSearchOutboxHandler worker;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TypesenseContainer typesense;

    private TypesenseProbe probe;

    @BeforeEach
    void setUp() {
        probe = new TypesenseProbe(typesense);
    }

    @AfterEach
    void putBack() {
        probe.resume();
        jdbc.update("DELETE FROM software WHERE id IN (?, ?)", ELITE, TETRIS);
        jdbc.update("DELETE FROM search_outbox");
    }

    private String currentCollection() throws Exception {
        Optional<String> target = probe.aliasTarget("software");
        assertThat(target).isPresent();
        return target.orElseThrow();
    }

    /** Software written past the outbox: only a rebuild brings it to the index. */
    private void insertUnindexed(UUID id, String title) {
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, ?, 'vector')", id, title);
    }

    @Test
    void theApplicationBuildsTheIndexOnStartWhenThereIsNone() throws Exception {
        assertThat(probe.aliasTarget("software")).isPresent();
    }

    @Test
    void loadsTheWholeArchiveIntoANewCollectionAndSwitchesTheAliasToIt() throws Exception {
        String before = currentCollection();
        insertUnindexed(ELITE, "Elite");

        rebuild.handle();

        assertThat(probe.aliasTarget("software")).isPresent().get().isNotEqualTo(before);
        assertThat(probe.software(ELITE)).isPresent();
    }

    @Test
    void aFailedRebuildLeavesTheSearchOnThePreviousCollection() throws Exception {
        insertUnindexed(ELITE, "Elite");
        rebuild.handle();
        String before = currentCollection();
        insertUnindexed(TETRIS, "Tetris");

        probe.freeze();
        rebuild.handle();
        probe.resume();

        assertThat(probe.aliasTarget("software")).contains(before);
        assertThat(probe.software(ELITE)).isPresent();
        assertThat(probe.software(TETRIS)).isEmpty();
    }

    @Test
    void theWorkerGoesOnAfterAFailedRebuild() throws Exception {
        rebuild.handle();
        probe.freeze();
        rebuild.handle();
        probe.resume();

        insertUnindexed(ELITE, "Elite");
        jdbc.update("INSERT INTO search_outbox (entity_type, entity_id) VALUES ('SOFTWARE', ?)", ELITE);
        worker.handle();

        assertThat(probe.software(ELITE)).isPresent();
    }
}

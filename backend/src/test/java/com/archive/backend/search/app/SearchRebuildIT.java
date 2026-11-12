package com.archive.backend.search.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchIndex;
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
    private static final UUID ZED = UUID.fromString("00000000-0000-0000-00fe-000000000011");
    private static final UUID ZED_LABEL = UUID.fromString("00000000-0000-0000-00fe-000000000012");
    private static final UUID SQUAD = UUID.fromString("00000000-0000-0000-00fe-000000000021");
    private static final UUID SQUAD_LABEL = UUID.fromString("00000000-0000-0000-00fe-000000000022");

    @Autowired
    private RebuildSearchIndexHandler rebuild;

    @Autowired
    private ProcessSearchOutboxHandler worker;

    @Autowired
    private SearchIndexOnStartup startup;

    @Autowired
    private SearchIndex index;

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
        jdbc.update("DELETE FROM label WHERE id IN (?, ?)", ZED_LABEL, SQUAD_LABEL);
        jdbc.update("DELETE FROM person WHERE id = ?", ZED);
        jdbc.update("DELETE FROM \"group\" WHERE id = ?", SQUAD);
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

    /** A person and a group written past the outbox: only a rebuild brings them to the index. */
    private void insertUnindexedOwners() {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Zed Zedson')", ZED);
        jdbc.update(
                "INSERT INTO label (id, name, person_id, is_display_name) VALUES (?, 'Zed', ?, TRUE)", ZED_LABEL, ZED);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Zed Squad')", SQUAD);
        jdbc.update(
                "INSERT INTO label (id, name, group_id, is_display_name) VALUES (?, 'ZS', ?, TRUE)",
                SQUAD_LABEL,
                SQUAD);
    }

    @Test
    void theApplicationBuildsTheIndexOnStartWhenThereIsNone() throws Exception {
        assertThat(probe.aliasTarget("software")).isPresent();
        assertThat(probe.aliasTarget("authors")).isPresent();
        assertThat(probe.aliasTarget("groups")).isPresent();
    }

    @Test
    void loadsThePeopleAndTheGroupsOfTheArchiveIntoNewCollections() throws Exception {
        String authorsBefore = probe.aliasTarget("authors").orElseThrow();
        String groupsBefore = probe.aliasTarget("groups").orElseThrow();
        insertUnindexedOwners();

        rebuild.handle();

        assertThat(probe.aliasTarget("authors")).isPresent().get().isNotEqualTo(authorsBefore);
        assertThat(probe.aliasTarget("groups")).isPresent().get().isNotEqualTo(groupsBefore);
        assertThat(probe.author(ZED))
                .isPresent()
                .get()
                .extracting(doc -> doc.get("name"))
                .isEqualTo("Zed");
        assertThat(probe.group(SQUAD))
                .isPresent()
                .get()
                .extracting(doc -> doc.get("name"))
                .isEqualTo("ZS");
    }

    @Test
    void aFailedRebuildLeavesEveryCollectionInUse() throws Exception {
        rebuild.handle();
        String software = currentCollection();
        String authors = probe.aliasTarget("authors").orElseThrow();
        String groups = probe.aliasTarget("groups").orElseThrow();

        probe.freeze();
        rebuild.handle();
        probe.resume();

        assertThat(probe.aliasTarget("software")).contains(software);
        assertThat(probe.aliasTarget("authors")).contains(authors);
        assertThat(probe.aliasTarget("groups")).contains(groups);
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

    @Test
    void aCollectionOfTheCurrentSchemaIsCurrent() {
        rebuild.handle();

        assertThat(index.isCurrent(SearchEntityType.SOFTWARE)).isTrue();
    }

    @Test
    void aCollectionOfAnOlderSchemaIsNotCurrent() throws Exception {
        probe.aliasAnOutdatedSoftwareCollection();

        assertThat(index.isCurrent(SearchEntityType.SOFTWARE)).isFalse();
    }

    @Test
    void collectionsOfPeopleAndGroupsOfTheCurrentSchemaAreCurrent() {
        rebuild.handle();

        assertThat(index.isCurrent(SearchEntityType.AUTHOR)).isTrue();
        assertThat(index.isCurrent(SearchEntityType.GROUP)).isTrue();
    }

    @Test
    void theApplicationRebuildsAnOlderCollectionOfPeopleOnStart() throws Exception {
        rebuild.handle();
        insertUnindexedOwners();
        probe.aliasAnOutdatedCollection("authors", "name");

        startup.onReady();

        assertThat(index.isCurrent(SearchEntityType.AUTHOR)).isTrue();
        assertThat(probe.author(ZED)).isPresent();
    }

    @Test
    void theApplicationRebuildsACollectionOfAnOlderSchemaOnStart() throws Exception {
        insertUnindexed(ELITE, "Elite");
        probe.aliasAnOutdatedSoftwareCollection();

        startup.onReady();

        assertThat(index.isCurrent(SearchEntityType.SOFTWARE)).isTrue();
        assertThat(probe.software(ELITE)).isPresent();
    }
}

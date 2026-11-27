package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.archive.backend.IntegrationTest;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SoftwareLinksMigrationIT {

    private static final String SCHEMA = "software_links_migration";
    private static final UUID SOFTWARE = UUID.fromString("00000000-0000-0000-0001-000000000001");
    private static final String HOMEPAGE = "https://www.example.org/elite/";
    private static final String STORE = "https://store.example.org/elite/";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void dropTheSchema() {
        jdbc.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
    }

    @Test
    void movesTheAssistantPageAndKeepsExistingLinksAndTheImportWithTheSameAddress() {
        flyway(MigrationVersion.fromVersion("42")).migrate();
        jdbc.update(
                "INSERT INTO software_links_migration.software (id, title, platform_code) VALUES (?, 'Elite', 'vector')",
                SOFTWARE);
        jdbc.update("""
                INSERT INTO software_links_migration.software_links (software_id, type, url) VALUES (?, 'store', ?)
                """, SOFTWARE, STORE);
        jdbc.update("""
                INSERT INTO software_links_migration.external_origin (origin, kind, external_id, entity_id, url)
                VALUES ('example.org', 'SOFTWARE', ?, ?, ?), ('example.org', 'SOFTWARE', '524', ?, ?)
                """, HOMEPAGE, SOFTWARE, HOMEPAGE, SOFTWARE, HOMEPAGE);

        flyway(MigrationVersion.LATEST).migrate();

        assertThat(jdbc.queryForList("""
                        SELECT type, url FROM software_links_migration.software_links WHERE software_id = ?
                        """, SOFTWARE))
                .extracting(link -> link.get("type"), link -> link.get("url"))
                .containsExactlyInAnyOrder(tuple("homepage", HOMEPAGE), tuple("store", STORE));
        assertThat(jdbc.queryForList("""
                        SELECT origin, external_id, url FROM software_links_migration.external_origin
                        WHERE kind = 'SOFTWARE' AND entity_id = ?
                        """, SOFTWARE))
                .extracting(
                        origin -> origin.get("origin"),
                        origin -> origin.get("external_id"),
                        origin -> origin.get("url"))
                .containsExactly(tuple("example.org", "524", HOMEPAGE));
    }

    private Flyway flyway(MigrationVersion target) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(SCHEMA)
                .locations("classpath:db/migration")
                .target(target)
                .load();
    }
}

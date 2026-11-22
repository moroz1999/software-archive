package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The migration that gives the runs of imports a log (docs/domain/imports.md): run on a schema of its own, migrated up
 * to the version before it. The reports of the runs before it are carried into their logs, without the time nobody
 * wrote down, and stay where they were too.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@IntegrationTest
class ImportRunLogMigrationIT {

    private static final String SCHEMA = "import_run_log_migration";

    private static final UUID RUN = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID SOFTWARE = UUID.fromString("00000000-0000-0000-0001-000000000001");
    private static final UUID LABEL = UUID.fromString("00000000-0000-0000-0004-000000000001");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    private Flyway flyway(String target) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(SCHEMA)
                .locations("classpath:db/migration")
                .target(target)
                .load();
    }

    @BeforeEach
    void migrateToTheVersionBeforeWithAReport() {
        flyway("35").migrate();
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run (id, archive, section, state, started_at, added, known,"
                        + " skipped, failed) VALUES (?, 'vektor-06c.ru', 'zagruzchiki', 'DONE', now(), 1, 2, 1, 1)",
                RUN);
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run_added (run_id, position, external_id, software_id, title)"
                        + " VALUES (?, 0, '599', ?, 'Disk Loader')",
                RUN,
                SOFTWARE);
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run_known (run_id, position, external_id, software_id, title,"
                        + " supplements) VALUES (?, 0, '754', ?, 'МикроДОС', 'CREDITS,DESCRIPTION'),"
                        + " (?, 1, '753', ?, 'CP/M', '')",
                RUN,
                SOFTWARE,
                RUN,
                SOFTWARE);
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run_skipped (run_id, position, external_id, url, title, reason,"
                        + " detail) VALUES (?, 0, '524', 'https://vektor-06c.ru/512-demo/', '512 demo', 'SECTION',"
                        + " 'utility-dlya-pc')",
                RUN);
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run_failed (run_id, position, external_id, url, reason)"
                        + " VALUES (?, 0, '598', 'https://vektor-06c.ru/g-dos/', 'Timed out')",
                RUN);
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run_author (run_id, position, kind, entity_id, name, created)"
                        + " VALUES (?, 0, 'LABEL', ?, 'Графов А.', true)",
                RUN,
                LABEL);
        jdbc.update(
                "INSERT INTO " + SCHEMA + ".import_run_place (run_id, position, kind, name)"
                        + " VALUES (?, 0, 'CITY', 'Ковров')",
                RUN);
    }

    @AfterEach
    void dropTheSchema() {
        jdbc.execute("DROP SCHEMA " + SCHEMA + " CASCADE");
    }

    @Test
    void carriesTheReportOfEachRunIntoItsLogWithoutATime() {
        flyway("36").migrate();

        List<Map<String, Object>> log = jdbc.queryForList(
                "SELECT kind, at, entry::text AS entry FROM " + SCHEMA
                        + ".import_run_log WHERE run_id = ? ORDER BY seq",
                RUN);

        assertThat(log)
                .extracting(line -> line.get("kind"))
                .containsExactly("ADDED", "KNOWN", "KNOWN", "SKIPPED", "FAILED", "AUTHOR", "PLACE");
        assertThat(log).extracting(line -> line.get("at")).containsOnlyNulls();
        assertThat(jdbc.queryForList(
                        "SELECT entry ->> 'title' FROM " + SCHEMA + ".import_run_log WHERE run_id = ? ORDER BY seq",
                        String.class,
                        RUN))
                .containsExactly("Disk Loader", "МикроДОС", "CP/M", "512 demo", null, null, null);
        assertThat(jdbc.queryForObject(
                        "SELECT entry::text FROM " + SCHEMA + ".import_run_log WHERE run_id = ? AND kind = 'KNOWN'"
                                + " ORDER BY seq LIMIT 1",
                        String.class,
                        RUN))
                .contains("\"supplements\": [\"CREDITS\", \"DESCRIPTION\"]")
                .contains("\"softwareId\": \"" + SOFTWARE + "\"");
        assertThat(jdbc.queryForObject(
                        "SELECT entry::text FROM " + SCHEMA + ".import_run_log WHERE run_id = ? AND kind = 'AUTHOR'",
                        String.class,
                        RUN))
                .contains("\"kind\": \"LABEL\"")
                .contains("\"entityId\": \"" + LABEL + "\"")
                .contains("\"created\": true");
    }

    @Test
    void keepsTheReportsWhereTheyWere() {
        flyway("36").migrate();

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM " + SCHEMA + ".import_run_known WHERE run_id = ?", Integer.class, RUN))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM " + SCHEMA + ".import_run_author WHERE run_id = ?", Integer.class, RUN))
                .isOne();
    }
}

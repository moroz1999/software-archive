package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
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
 * The migration that takes the days of group memberships away (docs/domain/authors.md#group-membership): run on a schema of its
 * own, migrated up to the version before it. It drops the days only while no membership has any, so none is lost.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@IntegrationTest
class MembershipsWithoutDatesMigrationIT {

    private static final String SCHEMA = "member_dates_migration";

    private static final UUID CREW = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID JANE = UUID.fromString("00000000-0000-0000-0000-000000000002");

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
    void migrateToTheVersionBefore() {
        flyway("34").migrate();
        jdbc.update("INSERT INTO member_dates_migration.\"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("INSERT INTO member_dates_migration.person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
    }

    @AfterEach
    void dropTheSchema() {
        jdbc.execute("DROP SCHEMA " + SCHEMA + " CASCADE");
    }

    private int dateColumns() {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = 'group_member'"
                        + " AND column_name IN ('start_date', 'end_date')",
                Integer.class,
                SCHEMA);
        return count == null ? 0 : count;
    }

    @Test
    void dropsTheDaysWhileNoMembershipHasAny() {
        jdbc.update(
                "INSERT INTO member_dates_migration.group_member (group_id, person_id, role_codes) VALUES (?, ?, '{}')",
                CREW,
                JANE);

        flyway("35").migrate();

        assertThat(dateColumns()).isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM member_dates_migration.group_member WHERE group_id = ?",
                        Integer.class,
                        CREW))
                .isOne();
    }

    @Test
    void stopsAndKeepsTheDaysWhenAMembershipHasOne() {
        jdbc.update(
                "INSERT INTO member_dates_migration.group_member (group_id, person_id, role_codes, start_date)"
                        + " VALUES (?, ?, '{}', DATE '1990-01-01')",
                CREW,
                JANE);

        assertThatThrownBy(() -> flyway("35").migrate()).hasMessageContaining("membership");

        assertThat(dateColumns()).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                        "SELECT start_date::text FROM member_dates_migration.group_member WHERE group_id = ?",
                        String.class,
                        CREW))
                .isEqualTo("1990-01-01");
    }
}

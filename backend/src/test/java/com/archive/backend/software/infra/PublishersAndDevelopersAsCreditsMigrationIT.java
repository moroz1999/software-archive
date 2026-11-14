package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The migration that turns the publishers and the developer companies of software into its credits with the roles
 * Publisher and Developer (docs/domain/software.md#credits): run on a schema of its own, migrated up to the version
 * before it and filled the old way. Its tables are named with the schema: the pool shares its connections.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PublishersAndDevelopersAsCreditsMigrationIT {

    private static final String SCHEMA = "credits_migration";

    private static final UUID ELITE = id(1);
    private static final UUID JANE = id(11);
    private static final UUID CREW = id(21);
    private static final UUID SOLAR = id(22);
    private static final UUID JP = id(31);
    private static final UUID SOL = id(32);

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
        flyway("21").migrate();
        jdbc.update(
                "INSERT INTO credits_migration.software (id, title, platform_code) VALUES (?, 'Elite', 'vector')",
                ELITE);
        jdbc.update("INSERT INTO credits_migration.person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO credits_migration.\"group\" (id, name) VALUES (?, 'Crew'), (?, 'Solar')", CREW, SOLAR);
        jdbc.update(
                "INSERT INTO credits_migration.label (id, name, person_id, group_id) VALUES (?, 'J.P.', ?, NULL), (?, 'Sol', NULL, ?)",
                JP,
                JANE,
                SOL,
                SOLAR);
    }

    @AfterEach
    void dropTheSchema() {
        jdbc.execute("DROP SCHEMA " + SCHEMA + " CASCADE");
    }

    private Map<UUID, List<String>> credits() {
        return jdbc
                .query(
                        "SELECT COALESCE(person_id, group_id, label_id) AS id, role_codes FROM credits_migration.software_contribution"
                                + " WHERE software_id = ?",
                        (rs, row) -> Map.entry(rs.getObject("id", UUID.class), List.of((String[])
                                rs.getArray("role_codes").getArray())),
                        ELITE)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Test
    void creditsThePublishersAndTheDevelopersWithTheirRolesBesideTheRolesTheyHad() {
        jdbc.update(
                "INSERT INTO credits_migration.software_publisher_author (software_id, author_id) VALUES (?, ?)",
                ELITE,
                JANE);
        jdbc.update(
                "INSERT INTO credits_migration.software_publisher_author_label (software_id, label_id) VALUES (?, ?)",
                ELITE,
                JP);
        jdbc.update(
                "INSERT INTO credits_migration.software_publisher_group (software_id, group_id) VALUES (?, ?)",
                ELITE,
                CREW);
        jdbc.update(
                "INSERT INTO credits_migration.software_publisher_group_label (software_id, label_id) VALUES (?, ?)",
                ELITE,
                SOL);
        jdbc.update(
                "INSERT INTO credits_migration.software_developer_group (software_id, group_id) VALUES (?, ?)",
                ELITE,
                CREW);
        jdbc.update(
                "INSERT INTO credits_migration.software_developer_label (software_id, label_id) VALUES (?, ?)",
                ELITE,
                SOL);
        jdbc.update(
                "INSERT INTO credits_migration.software_contribution (software_id, person_id, role_codes) VALUES (?, ?, ARRAY['code'])",
                ELITE,
                JANE);
        jdbc.update(
                "INSERT INTO credits_migration.software_contribution (software_id, label_id, role_codes) VALUES (?, ?, ARRAY['unknown'])",
                ELITE,
                JP);

        flyway("22").migrate();

        assertThat(credits())
                .containsOnly(
                        Map.entry(JANE, List.of("code", "publisher")),
                        Map.entry(JP, List.of("publisher")),
                        Map.entry(CREW, List.of("developer", "publisher")),
                        Map.entry(SOL, List.of("developer", "publisher")));
    }

    @Test
    void dropsTheListsOfPublishersAndDevelopers() {
        flyway("22").migrate();

        assertThat(jdbc.queryForList(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema = ?"
                                + " AND (table_name LIKE 'software_publisher%' OR table_name LIKE 'software_developer%')",
                        String.class, SCHEMA))
                .isEmpty();
    }

    private static UUID id(int number) {
        return UUID.fromString("00000000-0000-0000-00cc-%012d".formatted(number));
    }
}

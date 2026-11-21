package com.archive.backend.geo.infra;

import static com.archive.backend.geo.GeoTestRows.city;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * The migration that takes the cities out of the stub country "Unknown": a city whose country is not known has none
 * (docs/domain/geo.md); run again here on places stored the old way.
 */
@IntegrationTest
@Transactional
class CitiesOfNoKnownCountryMigrationIT {

    private static final String MIGRATION = "db/migration/V34__cities_of_no_known_country.sql";

    private static final UUID UNKNOWN = UUID.fromString("00000000-0000-0000-0000-00000000c001");
    private static final UUID KOVROV = UUID.fromString("00000000-0000-0000-00c3-000000000001");
    private static final UUID ZED = UUID.fromString("00000000-0000-0000-00c3-000000000011");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-00c3-000000000012");
    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-00c3-000000000021");
    private static final UUID TETRIS = UUID.fromString("00000000-0000-0000-00c3-000000000022");

    @Autowired
    private JdbcTemplate jdbc;

    private void migrate() throws IOException {
        jdbc.execute(new ClassPathResource(MIGRATION).getContentAsString(StandardCharsets.UTF_8));
    }

    /** Kovrov in "Unknown"; Zed from Kovrov credited on Elite, Bob from "Unknown" itself credited on Tetris. */
    private void storeTheOldWay() {
        city(jdbc, KOVROV, UNKNOWN, "Ковров", null);
        jdbc.update("INSERT INTO person (id) VALUES (?), (?)", ZED, BOB);
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary)"
                        + " VALUES ('PERSON', ?, ?, ?, TRUE), ('PERSON', ?, ?, NULL, TRUE)",
                ZED,
                UNKNOWN,
                KOVROV,
                BOB,
                UNKNOWN);
        jdbc.update(
                "INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector'), (?, 'Tetris', 'vector')",
                ELITE,
                TETRIS);
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id) VALUES (?, ?), (?, ?)",
                ELITE,
                ZED,
                TETRIS,
                BOB);
        jdbc.update("DELETE FROM search_outbox");
    }

    @Test
    void leavesACityOfTheCountryUnknownWithNoCountry() throws IOException {
        storeTheOldWay();

        migrate();

        assertThat(jdbc.queryForObject("SELECT country_id FROM city WHERE id = ?", UUID.class, KOVROV))
                .isNull();
    }

    @Test
    void keepsAPlaceInSuchACityByTheCityAlone() throws IOException {
        storeTheOldWay();

        migrate();

        assertThat(jdbc.queryForList(
                        "SELECT coalesce(country_id::text, '-') || ':' || city_id FROM entity_location WHERE entity_id = ?",
                        String.class,
                        ZED))
                .containsExactly("-:" + KOVROV);
    }

    @Test
    void leavesAPlaceInTheCountryUnknownItselfAsItIs() throws IOException {
        storeTheOldWay();

        migrate();

        assertThat(jdbc.queryForList(
                        "SELECT country_id FROM entity_location WHERE entity_id = ? AND city_id IS NULL",
                        UUID.class,
                        BOB))
                .containsExactly(UNKNOWN);
    }

    @Test
    void sendsTheSoftwareOfTheAuthorsOfSuchCitiesToTheSearchAgain() throws IOException {
        storeTheOldWay();

        migrate();

        assertThat(jdbc.queryForList("SELECT entity_id FROM search_outbox WHERE entity_type = 'SOFTWARE'", UUID.class))
                .containsExactly(ELITE);
    }
}

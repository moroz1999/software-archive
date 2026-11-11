package com.archive.backend.geo.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcGeoCatalog.class})
class JdbcGeoCatalogIT {

    private static final CountryId FINLAND = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final CityId HELSINKI = new CityId(UUID.fromString("00000000-0000-0000-0000-000000000011"));

    @Autowired
    private JdbcGeoCatalog catalog;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        jdbc.update("INSERT INTO country (id, name) VALUES (?, 'Finland')", FINLAND.value());
        jdbc.update(
                "INSERT INTO city (id, name, country_id) VALUES (?, 'Helsinki', ?)", HELSINKI.value(), FINLAND.value());
    }

    @Test
    void knowsACountryOfTheArchive() {
        assertThat(catalog.countryExists(FINLAND)).isTrue();
        assertThat(catalog.countryExists(new CountryId(UUID.randomUUID()))).isFalse();
    }

    @Test
    void knowsTheCountryOfACity() {
        assertThat(catalog.countryOf(HELSINKI)).contains(FINLAND);
        assertThat(catalog.countryOf(new CityId(UUID.randomUUID()))).isEmpty();
    }
}

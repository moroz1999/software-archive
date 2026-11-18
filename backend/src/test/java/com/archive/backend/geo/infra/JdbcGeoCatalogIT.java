package com.archive.backend.geo.infra;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
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
        country(jdbc, FINLAND.value(), "Финляндия", "Finland");
        city(jdbc, HELSINKI.value(), FINLAND.value(), "Хельсинки", "Helsinki");
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

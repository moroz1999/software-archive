package com.archive.backend.geo.infra;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JdbcGeoCatalogIT {

    private static final CountryId FINLAND = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final CityId HELSINKI = new CityId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final CityId KOVROV = new CityId(UUID.fromString("00000000-0000-0000-0000-000000000012"));

    @Autowired
    private JdbcGeoCatalog catalog;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        country(jdbc, FINLAND.value(), "Финляндия", "Finland");
        city(jdbc, HELSINKI.value(), FINLAND.value(), "Хельсинки", "Helsinki");
        city(jdbc, KOVROV.value(), null, "Ковров", null);
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

    @Test
    void knowsACityOfNoKnownCountryWithoutItsCountry() {
        assertThat(catalog.cityExists(KOVROV)).isTrue();
        assertThat(catalog.cityExists(HELSINKI)).isTrue();
        assertThat(catalog.cityExists(new CityId(UUID.randomUUID()))).isFalse();
        assertThat(catalog.countryOf(KOVROV)).isEmpty();
    }
}

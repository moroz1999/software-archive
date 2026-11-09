package com.archive.backend.geo.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcGeoReadRepository.class})
class JdbcGeoReadRepositoryIT {

    private static final UUID NORWAY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TAMPERE = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID HELSINKI = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID OSLO = UUID.fromString("00000000-0000-0000-0000-000000000013");

    @Autowired
    private JdbcGeoReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void readsTheCountriesByName() {
        insert();

        assertThat(reads.findCountries())
                .containsExactly(new CountryItem(FINLAND, "Finland"), new CountryItem(NORWAY, "Norway"));
    }

    @Test
    void readsTheCitiesOfACountryByName() {
        insert();

        assertThat(reads.findCities(FINLAND))
                .containsExactly(new CityItem(HELSINKI, "Helsinki"), new CityItem(TAMPERE, "Tampere"));
    }

    @Test
    void readsNoCitiesForAnUnknownCountry() {
        insert();

        assertThat(reads.findCities(UUID.randomUUID())).isEmpty();
    }

    private void insert() {
        jdbc.update("INSERT INTO country (id, name) VALUES (?, 'Norway'), (?, 'Finland')", NORWAY, FINLAND);
        jdbc.update(
                "INSERT INTO city (id, name, country_id) VALUES (?, 'Tampere', ?), (?, 'Helsinki', ?), (?, 'Oslo', ?)",
                TAMPERE,
                FINLAND,
                HELSINKI,
                FINLAND,
                OSLO,
                NORWAY);
    }
}

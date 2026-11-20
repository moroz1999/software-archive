package com.archive.backend.geo.infra;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JdbcGeoReadRepositoryIT {

    private static final UUID NORWAY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-0000-000000000002");
    /** The country of the schema for cities whose country is not known. */
    private static final UUID UNKNOWN = UUID.fromString("00000000-0000-0000-0000-00000000c001");

    private static final UUID LATVIA = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TAMPERE = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID HELSINKI = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID OSLO = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID ESPOO = UUID.fromString("00000000-0000-0000-0000-000000000014");

    @Autowired
    private JdbcGeoReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        country(jdbc, NORWAY, "Норвегия", "Norway");
        country(jdbc, FINLAND, "Финляндия", "Finland");
        city(jdbc, TAMPERE, FINLAND, "Тампере", "Tampere");
        city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
        city(jdbc, OSLO, NORWAY, "Осло", "Oslo");
    }

    @Test
    void readsTheCountriesByNameInTheLanguageAskedFor() {
        assertThat(reads.findCountries("en"))
                .containsExactly(
                        new CountryItem(FINLAND, "Finland"),
                        new CountryItem(NORWAY, "Norway"),
                        new CountryItem(UNKNOWN, "Unknown"));
        assertThat(reads.findCountries("ru"))
                .containsExactly(
                        new CountryItem(UNKNOWN, "Неизвестно"),
                        new CountryItem(NORWAY, "Норвегия"),
                        new CountryItem(FINLAND, "Финляндия"));
    }

    @Test
    void namesACountryWithoutANameInTheLanguageAskedForInRussian() {
        country(jdbc, LATVIA, "Латвия", null);

        assertThat(reads.findCountries("en"))
                .containsExactly(
                        new CountryItem(FINLAND, "Finland"),
                        new CountryItem(NORWAY, "Norway"),
                        new CountryItem(UNKNOWN, "Unknown"),
                        new CountryItem(LATVIA, "Латвия"));
    }

    @Test
    void readsTheCitiesOfACountryByNameInTheLanguageAskedFor() {
        assertThat(reads.findCities(FINLAND, "en"))
                .containsExactly(new CityItem(HELSINKI, "Helsinki"), new CityItem(TAMPERE, "Tampere"));
        assertThat(reads.findCities(FINLAND, "ru"))
                .containsExactly(new CityItem(TAMPERE, "Тампере"), new CityItem(HELSINKI, "Хельсинки"));
    }

    @Test
    void namesACityWithoutANameInTheLanguageAskedForInRussian() {
        city(jdbc, ESPOO, FINLAND, "Эспоо", null);

        assertThat(reads.findCities(FINLAND, "en"))
                .containsExactly(
                        new CityItem(HELSINKI, "Helsinki"),
                        new CityItem(TAMPERE, "Tampere"),
                        new CityItem(ESPOO, "Эспоо"));
    }

    @Test
    void readsNoCitiesForAnUnknownCountry() {
        assertThat(reads.findCities(UUID.randomUUID(), "en")).isEmpty();
    }

    @Test
    void deletesTheNamesWithTheCountryAndTheCity() {
        jdbc.update("DELETE FROM city WHERE country_id = ?", FINLAND);
        jdbc.update("DELETE FROM country WHERE id = ?", FINLAND);

        Integer names = jdbc.queryForObject(
                "SELECT (SELECT count(*) FROM country_name WHERE country_id = ?)"
                        + " + (SELECT count(*) FROM city_name WHERE city_id IN (?, ?))",
                Integer.class,
                FINLAND,
                HELSINKI,
                TAMPERE);
        assertThat(names).isZero();
    }
}

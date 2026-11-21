package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.geo.domain.GeoNameRepository;
import com.archive.backend.shared.domain.Language;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Names are compared whatever their case. */
@Repository
@RequiredArgsConstructor
public class JdbcGeoNameRepository implements GeoNameRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<CityId> cityNamed(GeoName name) {
        return jdbc.sql("""
                        SELECT city_id FROM city_name WHERE lower(name) = lower(:name)
                        ORDER BY city_id LIMIT 1
                        """)
                .param("name", name.value())
                .query(UUID.class)
                .optional()
                .map(CityId::new);
    }

    @Override
    public Optional<CountryId> countryNamed(GeoName name) {
        return jdbc.sql("""
                        SELECT country_id FROM country_name WHERE lower(name) = lower(:name)
                        ORDER BY country_id LIMIT 1
                        """)
                .param("name", name.value())
                .query(UUID.class)
                .optional()
                .map(CountryId::new);
    }

    @Override
    public void addCity(CityId city, @Nullable CountryId country, Language language, GeoName name) {
        jdbc.sql("INSERT INTO city (id, country_id) VALUES (:id, :country)")
                .param("id", city.value())
                .param("country", country == null ? null : country.value())
                .update();
        jdbc.sql("INSERT INTO city_name (city_id, language, name) VALUES (:id, :language, :name)")
                .param("id", city.value())
                .param("language", language.code())
                .param("name", name.value())
                .update();
    }

    @Override
    public void addCountry(CountryId country, Language language, GeoName name) {
        jdbc.sql("INSERT INTO country (id) VALUES (:id)")
                .param("id", country.value())
                .update();
        jdbc.sql("INSERT INTO country_name (country_id, language, name) VALUES (:id, :language, :name)")
                .param("id", country.value())
                .param("language", language.code())
                .param("name", name.value())
                .update();
    }
}

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
    public CityId addCity(CityId city, @Nullable CountryId country, Language language, GeoName name) {
        CityId added = new CityId(jdbc.sql("INSERT INTO city (id, country_id) VALUES (:id, :country) RETURNING id")
                .param("id", city.value())
                .param("country", country == null ? null : country.value())
                .query(UUID.class)
                .single());
        jdbc.sql("INSERT INTO city_name (city_id, language, name) VALUES (:id, :language, :name)")
                .param("id", added.value())
                .param("language", language.code())
                .param("name", name.value())
                .update();
        return added;
    }

    @Override
    public CountryId addCountry(CountryId country, Language language, GeoName name) {
        CountryId added = new CountryId(jdbc.sql("INSERT INTO country (id) VALUES (:id) RETURNING id")
                .param("id", country.value())
                .query(UUID.class)
                .single());
        jdbc.sql("INSERT INTO country_name (country_id, language, name) VALUES (:id, :language, :name)")
                .param("id", added.value())
                .param("language", language.code())
                .param("name", name.value())
                .update();
        return added;
    }
}

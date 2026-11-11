package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoCatalog;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcGeoCatalog implements GeoCatalog {

    private final JdbcClient jdbc;

    @Override
    public boolean countryExists(CountryId country) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM country WHERE id = :id)")
                .param("id", country.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public Optional<CountryId> countryOf(CityId city) {
        return jdbc.sql("SELECT country_id FROM city WHERE id = :id")
                .param("id", city.value())
                .query(UUID.class)
                .optional()
                .map(CountryId::new);
    }
}

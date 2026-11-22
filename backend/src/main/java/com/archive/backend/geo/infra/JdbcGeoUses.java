package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoUses;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcGeoUses implements GeoUses {

    private final JdbcClient jdbc;

    @Override
    public boolean countryInUse(CountryId country) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM city WHERE country_id = :id)
                            OR EXISTS (SELECT 1 FROM entity_location WHERE country_id = :id)
                        """).param("id", country.value()).query(Boolean.class).single();
    }

    @Override
    public boolean cityInUse(CityId city) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM entity_location WHERE city_id = :id)")
                .param("id", city.value())
                .query(Boolean.class)
                .single();
    }
}

package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CityLocations;
import com.archive.backend.geo.domain.CountryId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCityLocations implements CityLocations {

    private final JdbcClient jdbc;

    @Override
    public void moveTo(CityId city, @Nullable CountryId country) {
        jdbc.sql("UPDATE entity_location SET country_id = :country WHERE city_id = :city")
                .param("country", country == null ? null : country.value())
                .param("city", city.value())
                .update();
    }
}

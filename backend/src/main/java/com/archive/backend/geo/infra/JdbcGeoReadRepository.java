package com.archive.backend.geo.infra;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.GeoReadRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcGeoReadRepository implements GeoReadRepository {

    private final JdbcClient jdbc;

    @Override
    public List<CountryItem> findCountries() {
        return jdbc.sql("SELECT id, name FROM country ORDER BY name, id")
                .query((rs, row) -> new CountryItem(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }

    @Override
    public List<CityItem> findCities(UUID countryId) {
        return jdbc.sql("SELECT id, name FROM city WHERE country_id = :country ORDER BY name, id")
                .param("country", countryId)
                .query((rs, row) -> new CityItem(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }
}

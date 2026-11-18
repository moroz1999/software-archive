package com.archive.backend.geo.infra;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.GeoNameSql;
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
    public List<CountryItem> findCountries(String language) {
        return jdbc.sql("SELECT co.id, " + GeoNameSql.country("co.id")
                        + " AS name FROM country co ORDER BY name, co.id")
                .param("language", language)
                .query((rs, row) -> new CountryItem(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }

    @Override
    public List<CityItem> findCities(UUID countryId, String language) {
        return jdbc.sql("SELECT ci.id, " + GeoNameSql.city("ci.id")
                        + " AS name FROM city ci WHERE ci.country_id = :country ORDER BY name, ci.id")
                .param("country", countryId)
                .param("language", language)
                .query((rs, row) -> new CityItem(rs.getObject("id", UUID.class), rs.getString("name")))
                .list();
    }
}

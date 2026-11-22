package com.archive.backend.geo.infra;

import com.archive.backend.geo.read.CityDetails;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryDetails;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.GeoNameSql;
import com.archive.backend.geo.read.GeoReadRepository;
import com.archive.backend.geo.read.GeographyCity;
import com.archive.backend.geo.read.GeographyCountry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public List<GeographyCountry> findGeography(String language) {
        Map<UUID, List<GeographyCity>> cities = new HashMap<>();
        jdbc.sql("SELECT ci.id, ci.country_id, " + GeoNameSql.city("ci.id") + """
                         AS name,
                               (SELECT count(*) FROM entity_location el WHERE el.city_id = ci.id) AS places
                        FROM city ci WHERE ci.country_id IS NOT NULL ORDER BY name, ci.id
                        """)
                .param("language", language)
                .query((rs, row) -> {
                    cities.computeIfAbsent(rs.getObject("country_id", UUID.class), country -> new ArrayList<>())
                            .add(new GeographyCity(
                                    rs.getObject("id", UUID.class), rs.getString("name"), rs.getInt("places")));
                    return row;
                })
                .list();
        return jdbc.sql("SELECT co.id, " + GeoNameSql.country("co.id") + """
                         AS name,
                               (SELECT count(*) FROM entity_location el
                                LEFT JOIN city ci ON ci.id = el.city_id
                                WHERE COALESCE(el.country_id, ci.country_id) = co.id) AS places
                        FROM country co ORDER BY name, co.id
                        """)
                .param("language", language)
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    return new GeographyCountry(
                            id, rs.getString("name"), rs.getInt("places"), cities.getOrDefault(id, List.of()));
                })
                .list();
    }

    @Override
    public Optional<CountryDetails> findCountry(UUID id) {
        return jdbc.sql("SELECT id, latitude, longitude FROM country WHERE id = :id")
                .param("id", id)
                .query((rs, row) -> new CountryDetails(
                        id,
                        names("country", id),
                        rs.getObject("latitude", Double.class),
                        rs.getObject("longitude", Double.class)))
                .optional();
    }

    @Override
    public Optional<CityDetails> findCity(UUID id) {
        return jdbc.sql("SELECT id, country_id, latitude, longitude FROM city WHERE id = :id")
                .param("id", id)
                .query((rs, row) -> new CityDetails(
                        id,
                        names("city", id),
                        rs.getObject("country_id", UUID.class),
                        rs.getObject("latitude", Double.class),
                        rs.getObject("longitude", Double.class)))
                .optional();
    }

    /** The names of a country or a city by the codes of their languages; the table is one of the two, never input. */
    private Map<String, String> names(String table, UUID id) {
        Map<String, String> names = new HashMap<>();
        jdbc.sql("SELECT language, name FROM " + table + "_name WHERE " + table + "_id = :id")
                .param("id", id)
                .query((rs, row) -> names.put(rs.getString("language"), rs.getString("name")))
                .list();
        return names;
    }

    @Override
    public List<GeographyCity> findCitiesOfNoCountry(String language) {
        return jdbc.sql("SELECT ci.id, " + GeoNameSql.city("ci.id") + """
                         AS name,
                               (SELECT count(*) FROM entity_location el WHERE el.city_id = ci.id) AS places
                        FROM city ci WHERE ci.country_id IS NULL ORDER BY name, ci.id
                        """)
                .param("language", language)
                .query((rs, row) ->
                        new GeographyCity(rs.getObject("id", UUID.class), rs.getString("name"), rs.getInt("places")))
                .list();
    }
}

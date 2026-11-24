package com.archive.backend.geo.infra;

import com.archive.backend.geo.read.CityDetails;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryDetails;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.FoundPlace;
import com.archive.backend.geo.read.GeoDuplicates;
import com.archive.backend.geo.read.GeoNameSql;
import com.archive.backend.geo.read.GeoReadRepository;
import com.archive.backend.geo.read.GeographyCity;
import com.archive.backend.geo.read.GeographyCountry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final JdbcGeoDuplicates duplicates;

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

    @Override
    public GeoDuplicates findDuplicates(String language) {
        return duplicates.find(language);
    }

    @Override
    public List<FoundPlace> findPlaces(String name, String language) {
        String key = JdbcGeoDuplicates.key(":name");
        return jdbc.sql("""
                        WITH found AS (
                            SELECT 'country' AS kind, n.country_id AS id, NULL::uuid AS country_id,
                                   bool_or(%1$s = %2$s) AS exact,
                                   array_agg(n.language ORDER BY n.language) AS languages,
                                   array_agg(n.name ORDER BY n.language) AS names
                            FROM country_name n
                            GROUP BY n.country_id
                            HAVING bool_or(strpos(%1$s, %2$s) > 0)
                            UNION ALL
                            SELECT 'city', n.city_id, ci.country_id, bool_or(%1$s = %2$s),
                                   array_agg(n.language ORDER BY n.language), array_agg(n.name ORDER BY n.language)
                            FROM city_name n JOIN city ci ON ci.id = n.city_id
                            GROUP BY n.city_id, ci.country_id
                            HAVING bool_or(strpos(%1$s, %2$s) > 0)
                        )
                        SELECT f.kind, f.id, f.country_id, f.languages, f.names,
                               CASE f.kind WHEN 'country' THEN %3$s ELSE %4$s END AS name,
                               %5$s AS country
                        FROM found f
                        ORDER BY f.exact DESC, lower(CASE f.kind WHEN 'country' THEN %3$s ELSE %4$s END), f.id
                        LIMIT 50
                        """.formatted(
                                JdbcGeoDuplicates.key("n.name"),
                                key,
                                GeoNameSql.country("f.id"),
                                GeoNameSql.city("f.id"),
                                GeoNameSql.country("f.country_id")))
                .param("name", name)
                .param("language", language)
                .query((rs, row) -> {
                    String[] languages = (String[]) rs.getArray("languages").getArray();
                    String[] names = (String[]) rs.getArray("names").getArray();
                    Map<String, String> byLanguage = new LinkedHashMap<>();
                    for (int i = 0; i < languages.length; i++) {
                        byLanguage.put(languages[i], names[i]);
                    }
                    return new FoundPlace(
                            rs.getObject("id", UUID.class),
                            rs.getString("kind"),
                            rs.getString("name"),
                            byLanguage,
                            rs.getObject("country_id", UUID.class),
                            rs.getString("country"));
                })
                .list();
    }
}

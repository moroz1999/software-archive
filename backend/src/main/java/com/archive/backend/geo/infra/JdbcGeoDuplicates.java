package com.archive.backend.geo.infra;

import com.archive.backend.geo.read.CityDuplicates;
import com.archive.backend.geo.read.GeoDuplicate;
import com.archive.backend.geo.read.GeoDuplicates;
import com.archive.backend.geo.read.GeoNameSql;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Finds the countries and the cities named alike, for the read repository of geo. */
@Component
@RequiredArgsConstructor
class JdbcGeoDuplicates {

    private static final String KEY = key("n.name");

    /** A name compared as people mean it: letter case, "ё" and "е", spaces and hyphens aside; of an SQL expression. */
    static String key(String name) {
        return "regexp_replace(translate(lower(" + name + "), 'ё', 'е'), '[[:space:]-]', '', 'g')";
    }

    private final JdbcClient jdbc;

    private record Named(UUID id, @Nullable UUID country, String key) {}

    GeoDuplicates find(String language) {
        Map<UUID, GeoDuplicate> countries = new HashMap<>();
        jdbc.sql("SELECT co.id, " + GeoNameSql.country("co.id") + """
                         AS name,
                               (SELECT count(*) FROM entity_location el LEFT JOIN city ci ON ci.id = el.city_id
                                WHERE COALESCE(el.country_id, ci.country_id) = co.id) AS places
                        FROM country co
                        """)
                .param("language", language)
                .query((rs, row) -> countries.put(
                        rs.getObject("id", UUID.class),
                        new GeoDuplicate(
                                rs.getObject("id", UUID.class), rs.getString("name"), null, rs.getInt("places"))))
                .list();
        Map<UUID, GeoDuplicate> cities = new HashMap<>();
        Map<UUID, @Nullable UUID> countryOf = new HashMap<>();
        jdbc.sql("SELECT ci.id, ci.country_id, " + GeoNameSql.city("ci.id") + " AS name, "
                        + GeoNameSql.country("ci.country_id") + """
                         AS country,
                               (SELECT count(*) FROM entity_location el WHERE el.city_id = ci.id) AS places
                        FROM city ci
                        """)
                .param("language", language)
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    countryOf.put(id, rs.getObject("country_id", UUID.class));
                    return cities.put(
                            id,
                            new GeoDuplicate(id, rs.getString("name"), rs.getString("country"), rs.getInt("places")));
                })
                .list();
        List<Named> countryNames = jdbc.sql("SELECT n.country_id AS id, " + KEY + " AS key FROM country_name n")
                .query((rs, row) -> new Named(rs.getObject("id", UUID.class), null, rs.getString("key")))
                .list();
        List<Named> cityNames = jdbc.sql("SELECT n.city_id AS id, ci.country_id, " + KEY
                        + " AS key FROM city_name n JOIN city ci ON ci.id = n.city_id")
                .query((rs, row) -> new Named(
                        rs.getObject("id", UUID.class), rs.getObject("country_id", UUID.class), rs.getString("key")))
                .list();

        List<List<GeoDuplicate>> countryGroups = groups(countryNames).stream()
                .map(group -> sorted(group.stream()
                        .map(id -> Objects.requireNonNull(countries.get(id)))
                        .toList()))
                .toList();

        // Cities of a country are compared within it, together with the cities of no known country.
        Map<@Nullable UUID, List<Named>> byCountry = new LinkedHashMap<>();
        List<Named> ofNoCountry =
                cityNames.stream().filter(named -> named.country() == null).toList();
        cityNames.stream()
                .filter(named -> named.country() != null)
                .forEach(named -> byCountry
                        .computeIfAbsent(named.country(), country -> new ArrayList<>(ofNoCountry))
                        .add(named));
        byCountry.put(null, ofNoCountry);
        List<CityDuplicates> cityGroups = new ArrayList<>();
        byCountry.forEach((country, names) -> groups(names).stream()
                .filter(group -> country == null || group.stream().anyMatch(id -> country.equals(countryOf.get(id))))
                .forEach(group -> {
                    List<GeoDuplicate> members = sorted(group.stream()
                            .map(id -> Objects.requireNonNull(cities.get(id)))
                            .toList());
                    cityGroups.add(new CityDuplicates(
                            country == null
                                    ? null
                                    : Objects.requireNonNull(countries.get(country))
                                            .name(),
                            members));
                }));
        cityGroups.sort(Comparator.comparing((CityDuplicates group) -> Objects.requireNonNullElse(group.country(), ""))
                .thenComparing(group -> group.cities().getFirst().name()));
        return new GeoDuplicates(countryGroups, cityGroups);
    }

    /** The sets of ids that share a name, joined when one id is in two of them; only those of more than one id. */
    private static List<Set<UUID>> groups(Collection<Named> names) {
        Map<UUID, UUID> parent = new HashMap<>();
        Map<String, UUID> firstWithKey = new HashMap<>();
        for (Named named : names) {
            parent.putIfAbsent(named.id(), named.id());
            UUID first = firstWithKey.putIfAbsent(named.key(), named.id());
            if (first != null) {
                parent.put(root(parent, named.id()), root(parent, first));
            }
        }
        Map<UUID, Set<UUID>> sets = new HashMap<>();
        parent.keySet()
                .forEach(id -> sets.computeIfAbsent(root(parent, id), root -> new TreeSet<>())
                        .add(id));
        return sets.values().stream().filter(set -> set.size() > 1).toList();
    }

    private static UUID root(Map<UUID, UUID> parent, UUID id) {
        UUID current = id;
        UUID next = Objects.requireNonNull(parent.get(current));
        while (!next.equals(current)) {
            current = next;
            next = Objects.requireNonNull(parent.get(current));
        }
        return current;
    }

    private static List<GeoDuplicate> sorted(List<GeoDuplicate> group) {
        return group.stream()
                .sorted(Comparator.comparing((GeoDuplicate duplicate) -> duplicate.country() == null)
                        .thenComparing(GeoDuplicate::name)
                        .thenComparing(GeoDuplicate::id))
                .toList();
    }
}

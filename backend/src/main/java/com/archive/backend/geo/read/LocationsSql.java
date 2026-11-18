package com.archive.backend.geo.read;

/**
 * SQL of the locations of a person, a group or a label: the country, given directly or the one of the city, and the
 * city, named in the language of the {@code :language} parameter, of the owner with the id {@code :id}. The primary
 * location comes first; of a record from before the rule with none or several marked, the first of them.
 */
public final class LocationsSql {

    private LocationsSql() {}

    /** The locations of an owner of the type stored in {@code entity_location} ({@code PERSON}, {@code GROUP}, …). */
    public static String of(String entityType) {
        return """
                SELECT country_id, country, city_id, city,
                       ROW_NUMBER() OVER (ORDER BY marked DESC, country, city) = 1 AS is_primary
                FROM (SELECT co.id AS country_id, %s AS country, ci.id AS city_id, %s AS city,
                             COALESCE(l.is_primary, FALSE) AS marked
                      FROM entity_location l
                      LEFT JOIN city ci ON ci.id = l.city_id
                      LEFT JOIN country co ON co.id = COALESCE(l.country_id, ci.country_id)
                      WHERE l.entity_type = '%s' AND l.entity_id = :id) located
                ORDER BY marked DESC, country, city
                """.formatted(GeoNameSql.country("co.id"), GeoNameSql.city("ci.id"), entityType);
    }
}

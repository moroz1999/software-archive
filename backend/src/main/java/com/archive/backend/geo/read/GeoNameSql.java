package com.archive.backend.geo.read;

import com.archive.backend.shared.read.NameInLanguageSql;

/** SQL of the names of countries and cities in the language of the {@code :language} parameter, else in Russian. */
public final class GeoNameSql {

    private GeoNameSql() {}

    /** The name of the country with the id {@code id}, an SQL expression such as {@code co.id}; NULL for none. */
    public static String country(String id) {
        return NameInLanguageSql.of("country_name", "country_id", "name", id);
    }

    /** The name of the city with the id {@code id}, an SQL expression such as {@code ci.id}; NULL for none. */
    public static String city(String id) {
        return NameInLanguageSql.of("city_name", "city_id", "name", id);
    }
}

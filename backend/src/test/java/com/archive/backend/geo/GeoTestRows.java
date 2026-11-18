package com.archive.backend.geo;

import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;

/** Countries and cities put straight into the database for tests: named in Russian, and in English if given. */
public final class GeoTestRows {

    private GeoTestRows() {}

    public static void country(JdbcTemplate jdbc, UUID id, String russian, @Nullable String english) {
        jdbc.update("INSERT INTO country (id) VALUES (?)", id);
        names(jdbc, "country", id, russian, english);
    }

    public static void city(JdbcTemplate jdbc, UUID id, UUID country, String russian, @Nullable String english) {
        jdbc.update("INSERT INTO city (id, country_id) VALUES (?, ?)", id, country);
        names(jdbc, "city", id, russian, english);
    }

    private static void names(JdbcTemplate jdbc, String table, UUID id, String russian, @Nullable String english) {
        String insert = "INSERT INTO " + table + "_name (" + table + "_id, language, name) VALUES (?, ?, ?)";
        jdbc.update(insert, id, "ru", russian);
        if (english != null) {
            jdbc.update(insert, id, "en", english);
        }
    }
}

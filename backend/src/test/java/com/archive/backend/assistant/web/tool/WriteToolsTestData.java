package com.archive.backend.assistant.web.tool;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/** Rows the write tool tests start from, put straight into the database. */
public final class WriteToolsTestData {

    public static final UUID JANE = id(1);
    public static final UUID JD = id(2);
    public static final UUID JANE_ALT = id(3);
    public static final UUID CREW = id(4);
    public static final UUID CRW = id(5);
    public static final UUID GHOST = id(6);
    public static final UUID GAMES = id(41);
    public static final UUID FINLAND = id(51);
    public static final UUID HELSINKI = id(52);

    private WriteToolsTestData() {}

    /**
     * Jane Doe, known as JD (the display name) and Jane Alt; Crew, known also as CRW; Ghost, a label of nobody; the
     * category Games; Finland with Helsinki. All added by an administrator.
     */
    public static void insert(JdbcTemplate jdbc) {
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", CREW);
        jdbc.update("""
                INSERT INTO label (id, name, person_id, group_id, is_display_name) VALUES
                    (?, 'JD', ?, NULL, true), (?, 'Jane Alt', ?, NULL, false), (?, 'CRW', NULL, ?, false),
                    (?, 'Ghost', NULL, NULL, false)
                """, JD, JANE, JANE_ALT, JANE, CRW, CREW, GHOST);
        jdbc.update("INSERT INTO category (id, parent_id, position) VALUES (?, NULL, 0)", GAMES);
        jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'ru', 'Игры')", GAMES);
        jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'en', 'Games')", GAMES);
        com.archive.backend.geo.GeoTestRows.country(jdbc, FINLAND, "Финляндия", "Finland");
        com.archive.backend.geo.GeoTestRows.city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
    }

    public static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}

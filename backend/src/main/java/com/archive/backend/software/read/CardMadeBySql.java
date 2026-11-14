package com.archive.backend.software.read;

/**
 * Who made software, as its card names them (docs/domain/software.md#card): those credited as its developers, or when
 * there are none, everyone credited but those who only published it; one SQL array of names in name order. A group or
 * a person is named by their main name, a label by itself. Shared by every read of cards, so the latest list and the
 * catalog name the same.
 */
public final class CardMadeBySql {

    private CardMadeBySql() {}

    /** The main name of a person or a group of the query around it. */
    private static String mainName(String column, String ownName, String alias) {
        return """
                COALESCE((SELECT m.name FROM label m WHERE m.%1$s = %3$s.id
                          ORDER BY m.is_display_name DESC NULLS LAST, lower(m.name), m.id LIMIT 1), %3$s.%2$s)""".formatted(column, ownName, alias);
    }

    /** The names of the credits of the software of the alias in the query around it that meet the condition. */
    private static String names(String software, String condition) {
        return """
                ARRAY(SELECT n.name FROM (
                    SELECT COALESCE(l.name, %3$s, %4$s) AS name
                    FROM software_contribution c
                    LEFT JOIN label l ON l.id = c.label_id
                    LEFT JOIN person p ON p.id = c.person_id
                    LEFT JOIN "group" g ON g.id = c.group_id
                    WHERE c.software_id = %1$s.id AND %2$s) n
                    WHERE n.name IS NOT NULL
                    ORDER BY lower(n.name))""".formatted(
                software, condition, mainName("person_id", "real_name", "p"), mainName("group_id", "name", "g"));
    }

    /** The array expression for the software of the alias in the query around it. */
    public static String of(String software) {
        return "COALESCE(NULLIF(" + names(software, "'developer' = ANY (c.role_codes)") + ", ARRAY[]::text[]), "
                + names(software, "c.role_codes <> ARRAY['publisher']") + ")";
    }
}

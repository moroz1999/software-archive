package com.archive.backend.software.read;

/**
 * Who made software, as its card names them (docs/domain/software.md#card): its developer companies, or when it has
 * none, its credited authors; one SQL array of names in name order. A group or a person is named by their main name,
 * a label by itself. Shared by every read of cards, so the latest list and the catalog name the same.
 */
public final class CardMadeBySql {

    private CardMadeBySql() {}

    /** The main name of the group of the alias in the query around it. */
    private static String groupName(String group) {
        return """
                COALESCE((SELECT m.name FROM label m WHERE m.group_id = %1$s.id
                          ORDER BY m.is_display_name DESC NULLS LAST, lower(m.name), m.id LIMIT 1), %1$s.name)""".formatted(group);
    }

    /** The main name of the person of the alias in the query around it. */
    private static String personName(String person) {
        return """
                COALESCE((SELECT m.name FROM label m WHERE m.person_id = %1$s.id
                          ORDER BY m.is_display_name DESC NULLS LAST, lower(m.name), m.id LIMIT 1), %1$s.real_name)""".formatted(person);
    }

    /** The array expression for the software of the alias in the query around it. */
    public static String of(String software) {
        return """
                COALESCE(
                    NULLIF(ARRAY(SELECT n.name FROM (
                        SELECT %2$s AS name
                        FROM software_developer_group d JOIN "group" g ON g.id = d.group_id
                        WHERE d.software_id = %1$s.id
                        UNION ALL
                        SELECT l.name
                        FROM software_developer_label d JOIN label l ON l.id = d.label_id
                        WHERE d.software_id = %1$s.id) n
                        ORDER BY lower(n.name)), ARRAY[]::text[]),
                    ARRAY(SELECT n.name FROM (
                        SELECT COALESCE(l.name, %3$s) AS name
                        FROM software_contribution c
                        LEFT JOIN label l ON l.id = c.label_id
                        LEFT JOIN person p ON p.id = c.person_id
                        WHERE c.software_id = %1$s.id) n
                        WHERE n.name IS NOT NULL
                        ORDER BY lower(n.name)))""".formatted(software, groupName("g"), personName("p"));
    }
}

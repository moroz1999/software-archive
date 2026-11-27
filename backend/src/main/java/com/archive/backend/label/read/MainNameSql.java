package com.archive.backend.label.read;

/**
 * The main name of people and groups as SQL common table expressions (docs/domain/authors.md): of a person the label
 * marked as the display name, else the first of their labels by name, else the real name; of a group its own name,
 * so its {@code main_label_id} is always null. Joined after {@code WITH}, separated by commas, they give
 * {@code named_person (person_id, main_name, main_label_id, real_name, notes)} and
 * {@code named_group (group_id, main_name, main_label_id, group_name, notes)}.
 */
public final class MainNameSql {

    public static final String PEOPLE = """
            person_main_label AS (
                SELECT DISTINCT ON (l.person_id) l.person_id, l.id, l.name
                FROM label l
                WHERE l.person_id IS NOT NULL
                ORDER BY l.person_id, (l.is_display_name IS TRUE) DESC, lower(l.name), l.id
            ), named_person AS (
                SELECT p.id AS person_id, COALESCE(m.name, p.real_name) AS main_name, m.id AS main_label_id,
                       p.real_name, p.notes
                FROM person p
                LEFT JOIN person_main_label m ON m.person_id = p.id
            )
            """;

    public static final String GROUPS = """
            named_group AS (
                SELECT g.id AS group_id, g.name AS main_name, NULL::uuid AS main_label_id, g.name AS group_name, g.notes
                FROM "group" g
            )
            """;

    private MainNameSql() {}
}

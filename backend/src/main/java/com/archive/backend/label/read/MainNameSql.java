package com.archive.backend.label.read;

/**
 * The main name of people and groups as SQL common table expressions (docs/domain/authors.md): the label marked as
 * the display name, else the first of their labels by name, else their own name. Joined after {@code WITH}, separated
 * by commas, they give {@code named_person (person_id, main_name, main_label_id, real_name, notes)} and
 * {@code named_group (group_id, main_name, main_label_id, group_name, notes)}.
 */
public final class MainNameSql {

    public static final String PEOPLE = """
            person_main_label AS (
                SELECT DISTINCT ON (l.person_id) l.person_id, l.id, l.name
                FROM label l
                WHERE l.person_id IS NOT NULL
                ORDER BY l.person_id, l.is_display_name DESC NULLS LAST, lower(l.name), l.id
            ), named_person AS (
                SELECT p.id AS person_id, COALESCE(m.name, p.real_name) AS main_name, m.id AS main_label_id,
                       p.real_name, p.notes
                FROM person p
                LEFT JOIN person_main_label m ON m.person_id = p.id
            )
            """;

    public static final String GROUPS = """
            group_main_label AS (
                SELECT DISTINCT ON (l.group_id) l.group_id, l.id, l.name
                FROM label l
                WHERE l.group_id IS NOT NULL
                ORDER BY l.group_id, l.is_display_name DESC NULLS LAST, lower(l.name), l.id
            ), named_group AS (
                SELECT g.id AS group_id, COALESCE(m.name, g.name) AS main_name, m.id AS main_label_id,
                       g.name AS group_name, g.notes
                FROM "group" g
                LEFT JOIN group_main_label m ON m.group_id = g.id
            )
            """;

    private MainNameSql() {
    }
}

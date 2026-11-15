package com.archive.backend.software.infra;

import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceCatalog;
import com.archive.backend.software.read.SoftwareReferenceLookup;
import com.archive.backend.software.read.SoftwareReferenceOption;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Current people, groups and labels, for validation of credits and for the lookup of the form. A label of nobody is
 * credited and offered as a label of a person without an owner: imported credits name such labels.
 */
@Repository
@RequiredArgsConstructor
public class JdbcSoftwareReferences implements SoftwareReferenceCatalog, SoftwareReferenceLookup {

    private static final String ENTRIES = "WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS + """
            , entries AS (
                SELECT p.person_id AS id, p.person_id AS owner_id, 'author' AS type, p.main_name AS title,
                       concat_ws(' ', p.main_name, p.real_name) AS search_text
                FROM named_person p
                WHERE p.main_name IS NOT NULL
                UNION ALL
                SELECT l.id, p.person_id, 'author-label',
                       l.name || CASE WHEN p.real_name IS NULL OR p.real_name = l.name THEN '' ELSE ' — ' || p.real_name END,
                       concat_ws(' ', l.name, p.main_name, p.real_name)
                FROM label l
                JOIN named_person p ON p.person_id = l.person_id
                UNION ALL
                SELECT l.id, NULL::uuid, 'author-label', l.name, l.name
                FROM label l
                WHERE l.person_id IS NULL AND l.group_id IS NULL
                UNION ALL
                SELECT g.group_id, g.group_id, 'group', g.main_name, concat_ws(' ', g.main_name, g.group_name)
                FROM named_group g
                UNION ALL
                SELECT l.id, g.group_id, 'group-label',
                       l.name || CASE WHEN g.group_name = l.name THEN '' ELSE ' — ' || g.group_name END,
                       concat_ws(' ', l.name, g.main_name, g.group_name)
                FROM label l
                JOIN named_group g ON g.group_id = l.group_id
            )
            """;

    private final JdbcClient jdbc;

    @Override
    public boolean contains(SoftwareReference reference) {
        String sql = switch (reference.type()) {
            case AUTHOR -> "SELECT EXISTS (SELECT 1 FROM person WHERE id = :id)";
            case AUTHOR_LABEL -> "SELECT EXISTS (SELECT 1 FROM label WHERE id = :id AND group_id IS NULL)";
            case GROUP -> "SELECT EXISTS (SELECT 1 FROM \"group\" WHERE id = :id)";
            case GROUP_LABEL -> "SELECT EXISTS (SELECT 1 FROM label WHERE id = :id AND group_id IS NOT NULL)";
        };
        return jdbc.sql(sql)
                .param("id", reference.id().value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public List<SoftwareReferenceOption> find(String query, Set<String> types, int limit) {
        return jdbc.sql(ENTRIES + """
                        SELECT id, owner_id, type, title
                        FROM entries
                        WHERE type IN (:types) AND lower(search_text) LIKE :phrase
                        ORDER BY CASE WHEN lower(title) LIKE :prefix THEN 0 ELSE 1 END, lower(title), type, id
                        LIMIT :limit
                        """)
                .param("types", types)
                .param("phrase", "%" + query.toLowerCase(java.util.Locale.ROOT) + "%")
                .param("prefix", query.toLowerCase(java.util.Locale.ROOT) + "%")
                .param("limit", limit)
                .query((rs, row) -> new SoftwareReferenceOption(
                        rs.getObject("id", java.util.UUID.class),
                        rs.getObject("owner_id", java.util.UUID.class),
                        rs.getString("type"),
                        rs.getString("title")))
                .list();
    }
}

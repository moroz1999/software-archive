package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.SqlArrays.list;

import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.domain.SoftwareDocuments;
import com.archive.backend.software.read.CardMadeBySql;
import com.archive.backend.software.read.CardScreenshotsSql;
import com.archive.backend.software.read.ReleaseDateSql;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Builds the software documents of the search with plain SQL: the categories of software with every category above
 * them, and the countries of the people and groups credited on it by themselves or by a label, by any of their
 * locations, for the roles of the software itself (docs/domain/search.md#software-catalog); and what its card shows.
 */
@Repository
@RequiredArgsConstructor
public class JdbcSoftwareDocuments implements SoftwareDocuments {

    /**
     * The roles of work on a copy of software rather than on the software itself: cracks, adaptations, translations,
     * help. Who had only these gives the software no country.
     */
    private static final String UNOFFICIAL_ROLES = "'release', 'intro_code', 'intro_graphics', 'intro_music',"
            + " 'adaptation', 'restoring', 'localization', 'translation', 'guest', 'support', 'testing'";

    private static final String SELECT = """
            SELECT s.id, s.title, s.platform_code, s.release_year AS year,
            """ + ReleaseDateSql.of("s") + """
             AS release_date,
                   ARRAY(SELECT t.title FROM software_alternative_titles t
                         WHERE t.software_id = s.id ORDER BY t.title) AS alternative_titles,
                   ARRAY(WITH RECURSIVE branch (id, parent_id) AS (
                             SELECT c.id, c.parent_id FROM software_category sc
                             JOIN category c ON c.id = sc.category_id
                             WHERE sc.software_id = s.id
                             UNION
                             SELECT p.id, p.parent_id FROM category p JOIN branch b ON p.id = b.parent_id)
                         SELECT DISTINCT id FROM branch) AS categories,
                   ARRAY(SELECT DISTINCT COALESCE(el.country_id, city.country_id)
                         FROM software_contribution sc
                         LEFT JOIN label l ON l.id = sc.label_id
                         JOIN entity_location el
                           ON (el.entity_type = 'PERSON' AND el.entity_id = COALESCE(sc.person_id, l.person_id))
                           OR (el.entity_type = 'GROUP' AND el.entity_id = COALESCE(sc.group_id, l.group_id))
                         LEFT JOIN city ON city.id = el.city_id
                         WHERE sc.software_id = s.id
                           AND NOT sc.role_codes <@ ARRAY[""" + UNOFFICIAL_ROLES + """
            ]
                           AND COALESCE(el.country_id, city.country_id) IS NOT NULL) AS countries,
            """
            + CardScreenshotsSql.of("s") + """
             AS screenshots,
            """ + CardMadeBySql.of("s") + """
             AS made_by
            FROM software s
            """;

    private final JdbcClient jdbc;

    @Override
    public Optional<SoftwareDocument> of(UUID software) {
        return jdbc.sql(SELECT + " WHERE s.id = :id")
                .param("id", software)
                .query(JdbcSoftwareDocuments::document)
                .optional();
    }

    @Override
    public List<SoftwareDocument> after(@Nullable UUID after, int limit) {
        return jdbc.sql(SELECT + " WHERE :after::uuid IS NULL OR s.id > :after::uuid ORDER BY s.id LIMIT :limit")
                .param("after", after)
                .param("limit", limit)
                .query(JdbcSoftwareDocuments::document)
                .list();
    }

    private static SoftwareDocument document(ResultSet rs, int row) throws SQLException {
        return new SoftwareDocument(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("release_date"),
                list(rs.getArray("alternative_titles"), String.class),
                rs.getString("platform_code"),
                rs.getObject("year", Integer.class),
                list(rs.getArray("categories"), UUID.class),
                list(rs.getArray("countries"), UUID.class),
                list(rs.getArray("screenshots"), UUID.class),
                list(rs.getArray("made_by"), String.class));
    }
}

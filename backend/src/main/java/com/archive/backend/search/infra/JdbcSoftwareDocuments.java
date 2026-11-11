package com.archive.backend.search.infra;

import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.domain.SoftwareDocuments;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Builds the software documents of the search with plain SQL: the categories of software with every category above
 * them, and the countries of the owners of its credited labels, by any of their locations
 * (docs/domain/search.md#software-catalog).
 */
@Repository
@RequiredArgsConstructor
public class JdbcSoftwareDocuments implements SoftwareDocuments {

    private static final String SELECT = """
            SELECT s.id, s.title, s.release_date, s.platform_code,
                   EXTRACT(YEAR FROM s.release_date)::int AS year,
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
                         JOIN label l ON l.id = sc.label_id
                         JOIN entity_location el
                           ON (el.entity_type = 'PERSON' AND el.entity_id = l.person_id)
                           OR (el.entity_type = 'GROUP' AND el.entity_id = l.group_id)
                         LEFT JOIN city ON city.id = el.city_id
                         WHERE sc.software_id = s.id
                           AND COALESCE(el.country_id, city.country_id) IS NOT NULL) AS countries
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
                rs.getObject("release_date", LocalDate.class),
                list(rs.getArray("alternative_titles"), String.class),
                rs.getString("platform_code"),
                rs.getObject("year", Integer.class),
                list(rs.getArray("categories"), UUID.class),
                list(rs.getArray("countries"), UUID.class));
    }

    private static <T> List<T> list(Array array, Class<T> type) throws SQLException {
        return Arrays.stream((Object[]) array.getArray()).map(type::cast).toList();
    }
}

package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.SqlArrays.list;

import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.search.domain.AuthorDocument;
import com.archive.backend.search.domain.AuthorDocuments;
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
 * Builds the documents of people with plain SQL: under the main name, with every label, and with the platforms of the
 * software their labels are credited on. A person without any name is not an author and has none.
 */
@Repository
@RequiredArgsConstructor
public class JdbcAuthorDocuments implements AuthorDocuments {

    private static final String SELECT = "WITH " + MainNameSql.PEOPLE + """
            SELECT n.person_id AS id, n.main_name, n.real_name,
                   ARRAY(SELECT l.name FROM label l
                         WHERE l.person_id = n.person_id ORDER BY lower(l.name), l.id) AS labels,
                   ARRAY(SELECT DISTINCT s.platform_code FROM label l
                         JOIN software_contribution sc ON sc.label_id = l.id
                         JOIN software s ON s.id = sc.software_id
                         WHERE l.person_id = n.person_id ORDER BY s.platform_code) AS platforms
            FROM named_person n
            WHERE n.main_name IS NOT NULL
            """;

    private final JdbcClient jdbc;

    @Override
    public Optional<AuthorDocument> of(UUID person) {
        return jdbc.sql(SELECT + " AND n.person_id = :id")
                .param("id", person)
                .query(JdbcAuthorDocuments::document)
                .optional();
    }

    @Override
    public List<AuthorDocument> after(@Nullable UUID after, int limit) {
        return jdbc.sql(SELECT
                        + " AND (:after::uuid IS NULL OR n.person_id > :after::uuid) ORDER BY n.person_id LIMIT :limit")
                .param("after", after)
                .param("limit", limit)
                .query(JdbcAuthorDocuments::document)
                .list();
    }

    private static AuthorDocument document(ResultSet rs, int row) throws SQLException {
        return new AuthorDocument(
                rs.getObject("id", UUID.class),
                rs.getString("main_name"),
                list(rs.getArray("labels"), String.class),
                rs.getString("real_name"),
                list(rs.getArray("platforms"), String.class));
    }
}

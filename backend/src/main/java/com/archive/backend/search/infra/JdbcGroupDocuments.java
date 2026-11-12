package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.SqlArrays.list;

import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.search.domain.GroupDocument;
import com.archive.backend.search.domain.GroupDocuments;
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
 * Builds the documents of groups with plain SQL: under the main name, with every label, and with the platforms of the
 * software their labels are credited on.
 */
@Repository
@RequiredArgsConstructor
public class JdbcGroupDocuments implements GroupDocuments {

    private static final String SELECT = "WITH " + MainNameSql.GROUPS + """
            SELECT n.group_id AS id, n.main_name, n.group_name,
                   ARRAY(SELECT l.name FROM label l
                         WHERE l.group_id = n.group_id ORDER BY lower(l.name), l.id) AS labels,
                   ARRAY(SELECT DISTINCT s.platform_code FROM label l
                         JOIN software_contribution sc ON sc.label_id = l.id
                         JOIN software s ON s.id = sc.software_id
                         WHERE l.group_id = n.group_id ORDER BY s.platform_code) AS platforms
            FROM named_group n
            """;

    private final JdbcClient jdbc;

    @Override
    public Optional<GroupDocument> of(UUID group) {
        return jdbc.sql(SELECT + " WHERE n.group_id = :id")
                .param("id", group)
                .query(JdbcGroupDocuments::document)
                .optional();
    }

    @Override
    public List<GroupDocument> after(@Nullable UUID after, int limit) {
        return jdbc.sql(SELECT
                        + " WHERE :after::uuid IS NULL OR n.group_id > :after::uuid ORDER BY n.group_id LIMIT :limit")
                .param("after", after)
                .param("limit", limit)
                .query(JdbcGroupDocuments::document)
                .list();
    }

    private static GroupDocument document(ResultSet rs, int row) throws SQLException {
        return new GroupDocument(
                rs.getObject("id", UUID.class),
                rs.getString("main_name"),
                list(rs.getArray("labels"), String.class),
                rs.getString("group_name"),
                list(rs.getArray("platforms"), String.class));
    }
}

package com.archive.backend.category.infra;

import com.archive.backend.category.domain.CategoryCatalog;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryLine;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCategoryCatalog implements CategoryCatalog {

    private final JdbcClient jdbc;

    @Override
    public List<CategoryLine> linesOf(Set<CategoryId> categories) {
        if (categories.isEmpty()) {
            return List.of();
        }
        // Each category up to the top, itself first. Shared locks keep the lines from being deleted or moved until the
        // software put in their categories is stored.
        Map<UUID, List<CategoryId>> lines = new LinkedHashMap<>();
        jdbc.sql("""
                        WITH RECURSIVE line (start, id, parent_id, depth) AS (
                            SELECT id, id, parent_id, 0 FROM category WHERE id IN (:ids)
                            UNION ALL
                            SELECT l.start, c.id, c.parent_id, l.depth + 1
                            FROM category c
                            JOIN line l ON c.id = l.parent_id
                        )
                        SELECT l.start, c.id
                        FROM category c
                        JOIN line l ON c.id = l.id
                        ORDER BY l.start, l.depth
                        FOR SHARE OF c
                        """)
                .param("ids", categories.stream().map(CategoryId::value).toList())
                .query(rs -> {
                    lines.computeIfAbsent(rs.getObject("start", UUID.class), start -> new ArrayList<>())
                            .add(new CategoryId(rs.getObject("id", UUID.class)));
                });
        return lines.values().stream()
                .map(line -> new CategoryLine(line.getFirst(), line.subList(1, line.size())))
                .toList();
    }
}

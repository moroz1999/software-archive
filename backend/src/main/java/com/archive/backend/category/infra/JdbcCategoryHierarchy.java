package com.archive.backend.category.infra;

import com.archive.backend.category.domain.CategoryHierarchy;
import com.archive.backend.category.domain.CategoryId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCategoryHierarchy implements CategoryHierarchy {

    private final JdbcClient jdbc;

    /**
     * Locks the line until the transaction commits: a parent is checked against a line no other change can move
     * meanwhile, so two moves cannot put two categories under each other (docs/backend.md).
     */
    @Override
    public Optional<List<CategoryId>> lineOf(CategoryId category) {
        List<CategoryId> line = jdbc.sql("""
                        WITH RECURSIVE line (id, parent_id, depth) AS (
                            SELECT id, parent_id, 0 FROM category WHERE id = :id
                            UNION ALL
                            SELECT c.id, c.parent_id, l.depth + 1 FROM category c JOIN line l ON c.id = l.parent_id
                        )
                        SELECT c.id FROM category c JOIN line l ON c.id = l.id ORDER BY l.depth FOR UPDATE OF c
                        """).param("id", category.value()).query(UUID.class).list().stream()
                .map(CategoryId::new)
                .toList();
        return line.isEmpty() ? Optional.empty() : Optional.of(line);
    }
}

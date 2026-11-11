package com.archive.backend.category.infra;

import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.read.CategoryReadRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCategoryReadRepository implements CategoryReadRepository {

    private final JdbcClient jdbc;

    private record Row(UUID id, @Nullable UUID parentId, String title) {}

    /**
     * The tree is small (a few hundred categories): read whole and put together here.
     */
    @Override
    public List<CategoryNode> findTree() {
        List<Row> rows = jdbc.sql("SELECT id, parent_id, title FROM category ORDER BY position, title")
                .query((rs, n) -> new Row(
                        rs.getObject("id", UUID.class), rs.getObject("parent_id", UUID.class), rs.getString("title")))
                .list();
        Map<@Nullable UUID, List<Row>> children = new HashMap<>();
        rows.forEach(row -> children.computeIfAbsent(row.parentId(), parent -> new ArrayList<>())
                .add(row));
        return nodes(null, children);
    }

    private static List<CategoryNode> nodes(@Nullable UUID parent, Map<@Nullable UUID, List<Row>> children) {
        return children.getOrDefault(parent, List.of()).stream()
                .map(row -> new CategoryNode(row.id(), row.title(), nodes(row.id(), children)))
                .toList();
    }

    @Override
    public Optional<CategoryDetails> findById(UUID id) {
        return jdbc.sql("SELECT id, title, parent_id FROM category WHERE id = :id")
                .param("id", id)
                .query((rs, n) -> new CategoryDetails(
                        rs.getObject("id", UUID.class), rs.getString("title"), rs.getObject("parent_id", UUID.class)))
                .optional();
    }
}

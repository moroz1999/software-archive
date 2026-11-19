package com.archive.backend.software.infra;

import com.archive.backend.software.read.ReleaseFileItem;
import com.archive.backend.software.read.ReleaseFileReadRepository;
import com.archive.backend.software.read.ReleaseFiles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcReleaseFileReadRepository implements ReleaseFileReadRepository {

    private final JdbcClient jdbc;

    /** The release and its whole tree in one read: a release without a tree comes as one row with no file. */
    @Override
    public Optional<ReleaseFiles> find(UUID softwareId, UUID releaseId) {
        List<Row> rows = jdbc.sql("""
                        SELECT r.archive_id IS NOT NULL AS archive,
                               f.id, f.parent_id, f.file_name, f.directory, f.executable
                        FROM software_release r
                        LEFT JOIN release_file f ON f.release_id = r.id
                        WHERE r.id = :release AND r.software_id = :software
                        ORDER BY f.parent_id NULLS FIRST, f.position
                        """)
                .param("release", releaseId)
                .param("software", softwareId)
                .query((rs, row) -> new Row(
                        rs.getBoolean("archive"),
                        rs.getObject("id", UUID.class),
                        rs.getObject("parent_id", UUID.class),
                        rs.getString("file_name"),
                        rs.getBoolean("directory"),
                        rs.getBoolean("executable")))
                .list();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<UUID, List<Row>> children = new HashMap<>();
        @Nullable Row root = null;
        for (Row row : rows) {
            if (row.id == null) {
                continue;
            }
            if (row.parent == null) {
                root = row;
            } else {
                children.computeIfAbsent(row.parent, unused -> new ArrayList<>())
                        .add(row);
            }
        }
        return Optional.of(new ReleaseFiles(rows.getFirst().archive, root == null ? null : toItem(root, children)));
    }

    private static ReleaseFileItem toItem(Row row, Map<UUID, List<Row>> children) {
        UUID id = Objects.requireNonNull(row.id);
        return new ReleaseFileItem(
                id,
                Objects.requireNonNull(row.name),
                row.directory,
                row.executable,
                children.getOrDefault(id, List.of()).stream()
                        .map(child -> toItem(child, children))
                        .toList());
    }

    private record Row(
            boolean archive,
            @Nullable UUID id,
            @Nullable UUID parent,
            @Nullable String name,
            boolean directory,
            boolean executable) {}
}

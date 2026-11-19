package com.archive.backend.software.infra;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.ReleaseFile;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseFileId;
import com.archive.backend.software.domain.ReleaseFileName;
import com.archive.backend.software.domain.ReleaseFileTrees;
import com.archive.backend.software.domain.ReleasedFileTree;
import com.archive.backend.software.domain.SoftwareId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcReleaseFileTrees implements ReleaseFileTrees {

    private final JdbcClient jdbc;

    @Override
    public List<ReleasedFileTree> holdingAnyOf(PlatformCode platform, Set<ReleaseFileHash> files) {
        if (files.isEmpty()) {
            return List.of();
        }
        List<Row> rows = jdbc.sql("""
                        SELECT r.software_id, f.release_id, f.id, f.parent_id, f.file_name, f.md5,
                               f.directory, f.executable, f.unreadable
                        FROM release_file f
                        JOIN software_release r ON r.id = f.release_id
                        JOIN software s ON s.id = r.software_id
                        WHERE s.platform_code = :platform
                          AND f.release_id IN (SELECT release_id FROM release_file WHERE md5 IN (:files))
                        ORDER BY f.release_id, f.parent_id NULLS FIRST, f.position
                        """)
                .param("platform", platform.value())
                .param("files", files.stream().map(ReleaseFileHash::value).toList())
                .query((rs, row) -> new Row(
                        rs.getObject("software_id", UUID.class),
                        rs.getObject("release_id", UUID.class),
                        rs.getObject("id", UUID.class),
                        rs.getObject("parent_id", UUID.class),
                        rs.getString("file_name"),
                        rs.getString("md5"),
                        rs.getBoolean("directory"),
                        rs.getBoolean("executable"),
                        rs.getBoolean("unreadable")))
                .list();
        Map<UUID, List<Row>> byRelease = new LinkedHashMap<>();
        rows.forEach(row -> byRelease
                .computeIfAbsent(row.release, unused -> new ArrayList<>())
                .add(row));
        List<ReleasedFileTree> trees = new ArrayList<>();
        for (List<Row> release : byRelease.values()) {
            Map<UUID, List<Row>> children = new HashMap<>();
            @Nullable Row root = null;
            for (Row row : release) {
                if (row.parent == null) {
                    root = row;
                } else {
                    children.computeIfAbsent(row.parent, unused -> new ArrayList<>())
                            .add(row);
                }
            }
            if (root != null) {
                trees.add(new ReleasedFileTree(new SoftwareId(root.software), toFile(root, children)));
            }
        }
        return List.copyOf(trees);
    }

    private static ReleaseFile toFile(Row row, Map<UUID, List<Row>> children) {
        return new ReleaseFile(
                new ReleaseFileId(row.id),
                new ReleaseFileName(row.name),
                row.md5 == null ? null : new ReleaseFileHash(row.md5),
                row.directory,
                row.executable,
                row.unreadable,
                children.getOrDefault(row.id, List.of()).stream()
                        .map(child -> toFile(child, children))
                        .toList());
    }

    private record Row(
            UUID software,
            UUID release,
            UUID id,
            @Nullable UUID parent,
            String name,
            @Nullable String md5,
            boolean directory,
            boolean executable,
            boolean unreadable) {}
}

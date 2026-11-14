package com.archive.backend.importer.infra;

import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcImportRunReadRepository implements ImportRunReadRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<ImportRunReport> findLatest() {
        return jdbc.sql("""
                        SELECT id, archive, section, state, started_at, ended_at, added, known, skipped, failed
                        FROM import_run ORDER BY started_at DESC, id LIMIT 1
                        """)
                .query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    Timestamp ended = rs.getTimestamp("ended_at");
                    return new ImportRunReport(
                            id,
                            rs.getString("archive"),
                            rs.getString("section"),
                            rs.getString("state"),
                            rs.getTimestamp("started_at").toInstant(),
                            ended == null ? null : ended.toInstant(),
                            rs.getInt("added"),
                            rs.getInt("known"),
                            rs.getInt("skipped"),
                            rs.getInt("failed"),
                            added(id),
                            failed(id));
                })
                .optional();
    }

    private List<ImportRunAddedEntry> added(UUID run) {
        return jdbc.sql("""
                        SELECT a.external_id, a.software_id, a.title, s.id IS NOT NULL AS present
                        FROM import_run_added a LEFT JOIN software s ON s.id = a.software_id
                        WHERE a.run_id = :run ORDER BY a.position
                        """)
                .param("run", run)
                .query((rs, row) -> new ImportRunAddedEntry(
                        rs.getString("external_id"),
                        rs.getObject("software_id", UUID.class),
                        rs.getString("title"),
                        rs.getBoolean("present")))
                .list();
    }

    private List<ImportRunFailedEntry> failed(UUID run) {
        return jdbc.sql("""
                        SELECT external_id, url, reason FROM import_run_failed
                        WHERE run_id = :run ORDER BY position
                        """)
                .param("run", run)
                .query((rs, row) -> new ImportRunFailedEntry(
                        rs.getString("external_id"), rs.getString("url"), rs.getString("reason")))
                .list();
    }
}

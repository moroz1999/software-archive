package com.archive.backend.importer.infra;

import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunPlaceEntry;
import com.archive.backend.importer.read.ImportRunProgressEntry;
import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
                        SELECT id, archive, section, state, started_at, ended_at, added, known, skipped, failed,
                               progress_position, progress_total, progress_url
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
                            failed(id),
                            known(id),
                            skipped(id),
                            authors(id),
                            places(id),
                            new ImportRunProgressEntry(
                                    rs.getInt("progress_position"),
                                    rs.getObject("progress_total", Integer.class),
                                    rs.getString("progress_url")));
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

    private List<ImportRunKnownEntry> known(UUID run) {
        return jdbc.sql("""
                        SELECT k.external_id, k.software_id, k.title, k.supplements, s.id IS NOT NULL AS present
                        FROM import_run_known k LEFT JOIN software s ON s.id = k.software_id
                        WHERE k.run_id = :run ORDER BY k.position
                        """)
                .param("run", run)
                .query((rs, row) -> new ImportRunKnownEntry(
                        rs.getString("external_id"),
                        rs.getObject("software_id", UUID.class),
                        rs.getString("title"),
                        rs.getBoolean("present"),
                        supplements(rs.getString("supplements"))))
                .list();
    }

    private List<ImportRunSkippedEntry> skipped(UUID run) {
        return jdbc.sql("""
                        SELECT external_id, url, title, reason, detail FROM import_run_skipped
                        WHERE run_id = :run ORDER BY position
                        """)
                .param("run", run)
                .query((rs, row) -> new ImportRunSkippedEntry(
                        rs.getString("external_id"),
                        rs.getString("url"),
                        rs.getString("title"),
                        code(rs.getString("reason")),
                        rs.getString("detail")))
                .list();
    }

    private List<ImportRunAuthorEntry> authors(UUID run) {
        return jdbc.sql("""
                        SELECT a.kind, a.entity_id, a.name, a.created,
                               CASE a.kind
                                   WHEN 'AUTHOR' THEN EXISTS (SELECT 1 FROM person p WHERE p.id = a.entity_id)
                                   WHEN 'GROUP' THEN EXISTS (SELECT 1 FROM "group" g WHERE g.id = a.entity_id)
                                   ELSE EXISTS (SELECT 1 FROM label l WHERE l.id = a.entity_id)
                               END AS present
                        FROM import_run_author a
                        WHERE a.run_id = :run ORDER BY a.position
                        """)
                .param("run", run)
                .query((rs, row) -> new ImportRunAuthorEntry(
                        code(rs.getString("kind")),
                        rs.getObject("entity_id", UUID.class),
                        rs.getString("name"),
                        rs.getBoolean("created"),
                        rs.getBoolean("present")))
                .list();
    }

    private List<ImportRunPlaceEntry> places(UUID run) {
        return jdbc.sql("SELECT kind, name FROM import_run_place WHERE run_id = :run ORDER BY position")
                .param("run", run)
                .query((rs, row) -> new ImportRunPlaceEntry(code(rs.getString("kind")), rs.getString("name")))
                .list();
    }

    private static List<String> supplements(String stored) {
        return stored.isEmpty()
                ? List.of()
                : Arrays.stream(stored.split(","))
                        .map(JdbcImportRunReadRepository::code)
                        .toList();
    }

    /** RELEASE_DATE → release-date: codes go to the frontend in lower case, words joined by hyphens. */
    private static String code(String name) {
        return name.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}

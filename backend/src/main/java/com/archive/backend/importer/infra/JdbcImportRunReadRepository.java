package com.archive.backend.importer.infra;

import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunDownloadedEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunListReadEntry;
import com.archive.backend.importer.read.ImportRunLogEntry;
import com.archive.backend.importer.read.ImportRunLogLine;
import com.archive.backend.importer.read.ImportRunPlaceEntry;
import com.archive.backend.importer.read.ImportRunProgressEntry;
import com.archive.backend.importer.read.ImportRunReadRepository;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcImportRunReadRepository implements ImportRunReadRepository {

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    @Override
    public Optional<ImportRunReport> findLatest(@Nullable Long after) {
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
                            new ImportRunProgressEntry(
                                    rs.getInt("progress_position"),
                                    rs.getObject("progress_total", Integer.class),
                                    rs.getString("progress_url")),
                            log(id, after));
                })
                .optional();
    }

    private List<ImportRunLogLine> log(UUID run, @Nullable Long after) {
        return jdbc.sql("""
                        SELECT l.seq, l.at, l.kind, l.entry::text AS entry,
                               CASE
                                   WHEN l.kind IN ('ADDED', 'KNOWN') THEN EXISTS (
                                       SELECT 1 FROM software s WHERE s.id = (l.entry ->> 'softwareId')::uuid)
                                   WHEN l.kind = 'AUTHOR' THEN CASE l.entry ->> 'kind'
                                       WHEN 'AUTHOR' THEN EXISTS (
                                           SELECT 1 FROM person p WHERE p.id = (l.entry ->> 'entityId')::uuid)
                                       WHEN 'GROUP' THEN EXISTS (
                                           SELECT 1 FROM "group" g WHERE g.id = (l.entry ->> 'entityId')::uuid)
                                       ELSE EXISTS (
                                           SELECT 1 FROM label b WHERE b.id = (l.entry ->> 'entityId')::uuid)
                                   END
                                   ELSE false
                               END AS present
                        FROM import_run_log l
                        WHERE l.run_id = :run AND (CAST(:after AS bigint) IS NULL OR l.seq > CAST(:after AS bigint))
                        ORDER BY l.seq
                        """)
                .param("run", run)
                .param("after", after)
                .query((rs, row) -> {
                    Timestamp at = rs.getTimestamp("at");
                    String kind = rs.getString("kind");
                    return new ImportRunLogLine(
                            rs.getLong("seq"),
                            at == null ? null : at.toInstant(),
                            code(kind),
                            entry(kind, tree(rs.getString("entry")), rs.getBoolean("present")));
                })
                .list();
    }

    private JsonNode tree(String entry) {
        try {
            return json.readTree(entry);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("A stored log line is not JSON", e);
        }
    }

    private static ImportRunLogEntry entry(String kind, JsonNode e, boolean present) {
        return switch (kind) {
            case "LIST" ->
                new ImportRunListReadEntry(
                        text(e, "url"),
                        e.get("number").asInt(),
                        e.hasNonNull("pages") ? e.get("pages").asInt() : null,
                        e.get("programs").asInt());
            case "DOWNLOAD" ->
                new ImportRunDownloadedEntry(
                        text(e, "externalId"),
                        text(e, "title"),
                        code(text(e, "file")),
                        text(e, "name"),
                        e.get("size").asLong());
            case "ADDED" ->
                new ImportRunAddedEntry(
                        text(e, "externalId"), UUID.fromString(text(e, "softwareId")), text(e, "title"), present);
            case "KNOWN" -> {
                List<String> supplements = new ArrayList<>();
                e.get("supplements").forEach(code -> supplements.add(code(code.asText())));
                yield new ImportRunKnownEntry(
                        text(e, "externalId"),
                        UUID.fromString(text(e, "softwareId")),
                        text(e, "title"),
                        present,
                        supplements);
            }
            case "SKIPPED" ->
                new ImportRunSkippedEntry(
                        text(e, "externalId"),
                        text(e, "url"),
                        text(e, "title"),
                        code(text(e, "reason")),
                        text(e, "detail"));
            case "FAILED" -> new ImportRunFailedEntry(text(e, "externalId"), text(e, "url"), text(e, "reason"));
            case "AUTHOR" ->
                new ImportRunAuthorEntry(
                        code(text(e, "kind")),
                        UUID.fromString(text(e, "entityId")),
                        text(e, "name"),
                        e.get("created").asBoolean(),
                        present);
            case "PLACE" -> new ImportRunPlaceEntry(code(text(e, "kind")), text(e, "name"));
            default -> throw new IllegalStateException("Unknown kind of a log line: " + kind);
        };
    }

    private static String text(JsonNode entry, String field) {
        return entry.get(field).asText();
    }

    /** RELEASE_DATE → release-date: codes go to the frontend in lower case, words joined by hyphens. */
    private static String code(String name) {
        return name.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}

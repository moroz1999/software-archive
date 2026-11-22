package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.run.ImportLogLine;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** A run with its row; the lines of its log are only ever appended, never read back into it. */
@Repository
@RequiredArgsConstructor
public class JpaImportRunRepository implements ImportRunRepository {

    private final SpringDataImportRunRepository springDataImportRunRepository;
    private final ImportRunMapper mapper;
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    @Override
    public Optional<ImportRun> findById(ImportRunId id) {
        return springDataImportRunRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<ImportRun> findRunning() {
        return springDataImportRunRepository.findByState(ImportRunState.RUNNING.name()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void save(ImportRun run) {
        springDataImportRunRepository.saveAndFlush(mapper.toEntity(run));
        for (ImportLogLine line : run.pullNewLog()) {
            jdbc.sql(
                            "INSERT INTO import_run_log (run_id, at, kind, entry) VALUES (:run, :at, :kind, CAST(:entry AS jsonb))")
                    .param("run", run.getId().value())
                    .param("at", Timestamp.from(line.at()))
                    .param("kind", ImportLogRows.kind(line.entry()))
                    .param("entry", text(line))
                    .update();
        }
    }

    private String text(ImportLogLine line) {
        try {
            return json.writeValueAsString(ImportLogRows.entry(line.entry()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("The log line cannot be written as JSON", e);
        }
    }
}

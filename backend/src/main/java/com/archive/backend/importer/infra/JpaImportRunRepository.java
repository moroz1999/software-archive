package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunRepository;
import com.archive.backend.importer.domain.run.ImportRunState;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaImportRunRepository implements ImportRunRepository {

    private final SpringDataImportRunRepository springDataImportRunRepository;
    private final ImportRunMapper mapper;

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
    }
}

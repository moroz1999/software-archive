package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.run.AddedProgram;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.vektor.VektorSection;
import java.util.ArrayList;
import org.mapstruct.Mapper;

/** A run of an import ↔ its JPA entity. */
@Mapper(componentModel = "spring")
public interface ImportRunMapper {

    default ImportRun toDomain(ImportRunEntity entity) {
        String section = entity.getSection();
        return new ImportRun(
                new ImportRunId(entity.getId()),
                new ImportScope(entity.getArchive(), section == null ? null : new VektorSection(section)),
                entity.getStartedAt(),
                ImportRunState.valueOf(entity.getState()),
                entity.getEndedAt(),
                new ImportSummary(entity.getAdded(), entity.getKnown(), entity.getSkipped(), entity.getFailed()),
                entity.getAddedPrograms().stream()
                        .map(row -> new AddedProgram(
                                row.getExternalId(), new ImportedEntryId(row.getSoftwareId()), row.getTitle()))
                        .toList(),
                entity.getFailedPrograms().stream()
                        .map(row ->
                                new FailedProgram(row.getExternalId(), new ExternalPage(row.getUrl()), row.getReason()))
                        .toList());
    }

    default ImportRunEntity toEntity(ImportRun run) {
        ImportRunEntity entity = new ImportRunEntity();
        VektorSection section = run.getScope().section();
        ImportSummary counts = run.summary();
        entity.setId(run.getId().value());
        entity.setArchive(run.getScope().archive());
        entity.setSection(section == null ? null : section.code());
        entity.setState(run.getState().name());
        entity.setStartedAt(run.getStartedAt());
        entity.setEndedAt(run.getEndedAt());
        entity.setAdded(counts.added());
        entity.setKnown(counts.known());
        entity.setSkipped(counts.skipped());
        entity.setFailed(counts.failed());
        entity.setAddedPrograms(new ArrayList<>(run.getAdded().stream()
                .map(program -> new ImportRunAddedRow(
                        program.externalId(), program.software().value(), program.title()))
                .toList()));
        entity.setFailedPrograms(new ArrayList<>(run.getFailed().stream()
                .map(program -> new ImportRunFailedRow(
                        program.externalId(), program.page().url(), program.reason()))
                .toList()));
        return entity;
    }
}

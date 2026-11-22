package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.vektor.VektorSection;
import org.jspecify.annotations.Nullable;
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
                new ImportProgress(
                        entity.getProgressPosition(), entity.getProgressTotal(), page(entity.getProgressUrl())));
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
        ExternalPage page = run.getProgress().page();
        entity.setProgressPosition(run.getProgress().position());
        entity.setProgressTotal(run.getProgress().total());
        entity.setProgressUrl(page == null ? null : page.url());
        return entity;
    }

    private static @Nullable ExternalPage page(@Nullable String url) {
        return url == null ? null : new ExternalPage(url);
    }
}

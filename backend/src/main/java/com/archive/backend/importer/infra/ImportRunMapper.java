package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.run.AddedPlace;
import com.archive.backend.importer.domain.run.AddedProgram;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.run.KnownProgram;
import com.archive.backend.importer.domain.run.SkipReason;
import com.archive.backend.importer.domain.run.SkippedProgram;
import com.archive.backend.importer.domain.run.TakenAuthor;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.software.domain.SoftwareSupplement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
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
                entity.getAddedPrograms().stream()
                        .map(row -> new AddedProgram(
                                row.getExternalId(), new ImportedEntryId(row.getSoftwareId()), row.getTitle()))
                        .toList(),
                entity.getFailedPrograms().stream()
                        .map(row ->
                                new FailedProgram(row.getExternalId(), new ExternalPage(row.getUrl()), row.getReason()))
                        .toList(),
                entity.getKnownPrograms().stream()
                        .map(row -> new KnownProgram(
                                row.getExternalId(),
                                new ImportedEntryId(row.getSoftwareId()),
                                row.getTitle(),
                                supplements(row.getSupplements())))
                        .toList(),
                entity.getSkippedPrograms().stream()
                        .map(row -> new SkippedProgram(
                                row.getExternalId(),
                                new ExternalPage(row.getUrl()),
                                row.getTitle(),
                                SkipReason.valueOf(row.getReason()),
                                row.getDetail()))
                        .toList(),
                entity.getAuthors().stream()
                        .map(row -> new TakenAuthor(
                                ExternalKind.valueOf(row.getKind()),
                                new ImportedEntryId(row.getEntityId()),
                                row.getName(),
                                row.isCreated()))
                        .toList(),
                entity.getPlaces().stream()
                        .map(row -> new AddedPlace(AddedPlace.Kind.valueOf(row.getKind()), row.getName()))
                        .toList(),
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
        entity.setAddedPrograms(new ArrayList<>(run.getAdded().stream()
                .map(program -> new ImportRunAddedRow(
                        program.externalId(), program.software().value(), program.title()))
                .toList()));
        entity.setFailedPrograms(new ArrayList<>(run.getFailed().stream()
                .map(program -> new ImportRunFailedRow(
                        program.externalId(), program.page().url(), program.reason()))
                .toList()));
        entity.setKnownPrograms(new ArrayList<>(run.getKnown().stream()
                .map(program -> new ImportRunKnownRow(
                        program.externalId(),
                        program.software().value(),
                        program.title(),
                        program.supplements().stream().map(Enum::name).sorted().collect(Collectors.joining(","))))
                .toList()));
        entity.setSkippedPrograms(new ArrayList<>(run.getSkipped().stream()
                .map(program -> new ImportRunSkippedRow(
                        program.externalId(),
                        program.page().url(),
                        program.title(),
                        program.reason().name(),
                        program.detail()))
                .toList()));
        entity.setAuthors(new ArrayList<>(run.getAuthors().stream()
                .map(author -> new ImportRunAuthorRow(
                        author.kind().name(), author.entity().value(), author.name(), author.created()))
                .toList()));
        entity.setPlaces(new ArrayList<>(run.getPlaces().stream()
                .map(place -> new ImportRunPlaceRow(place.kind().name(), place.name()))
                .toList()));
        ExternalPage page = run.getProgress().page();
        entity.setProgressPosition(run.getProgress().position());
        entity.setProgressTotal(run.getProgress().total());
        entity.setProgressUrl(page == null ? null : page.url());
        return entity;
    }

    private static Set<SoftwareSupplement> supplements(String codes) {
        return codes.isEmpty()
                ? Set.of()
                : Arrays.stream(codes.split(","))
                        .map(SoftwareSupplement::valueOf)
                        .collect(Collectors.toSet());
    }

    private static @Nullable ExternalPage page(@Nullable String url) {
        return url == null ? null : new ExternalPage(url);
    }
}

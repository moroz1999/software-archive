package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.run.AddedPlace;
import com.archive.backend.importer.domain.run.AddedProgram;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportProgress;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.run.KnownProgram;
import com.archive.backend.importer.domain.run.SkipReason;
import com.archive.backend.importer.domain.run.SkippedProgram;
import com.archive.backend.importer.domain.run.TakenAuthor;
import com.archive.backend.importer.domain.run.TakenAuthors;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.software.domain.SoftwareSupplement;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JpaImportRunRepositoryIT {

    private static final Instant START = Instant.parse("2026-11-14T10:00:00Z");
    private static final AddedProgram DISK_LOADER =
            new AddedProgram("599", new ImportedEntryId(UUID.randomUUID()), "Disk Loader");
    private static final FailedProgram G_DOS =
            new FailedProgram("598", new ExternalPage("https://vektor-06c.ru/g-dos/"), "Timed out");
    private static final KnownProgram MICRODOS = new KnownProgram(
            "754",
            new ImportedEntryId(UUID.randomUUID()),
            "МикроДОС",
            Set.of(SoftwareSupplement.DESCRIPTION, SoftwareSupplement.CREDITS));
    private static final SkippedProgram DEMO = new SkippedProgram(
            "524",
            new ExternalPage("https://vektor-06c.ru/512-demo/"),
            "512 demo",
            SkipReason.SECTION,
            "utility-dlya-pc");
    private static final TakenAuthor GRAFOV =
            new TakenAuthor(ExternalKind.LABEL, new ImportedEntryId(UUID.randomUUID()), "Графов А.", true);
    private static final TakenAuthor CREW =
            new TakenAuthor(ExternalKind.GROUP, new ImportedEntryId(UUID.randomUUID()), "Crew", false);
    private static final AddedPlace KOVROV = new AddedPlace(AddedPlace.Kind.CITY, "Ковров");

    @Autowired
    private JpaImportRunRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void keepsARunWithItsReportInOrder() {
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        ImportRun run = ImportRun.start(id, new ImportScope("vektor-06c.ru", new VektorSection("zagruzchiki")), START);
        repository.save(run);
        run.record(new ImportOutcome.Failed(G_DOS));
        run.record(new ImportOutcome.Known(MICRODOS, new TakenAuthors(List.of(CREW), List.of())));
        run.record(new ImportOutcome.Skipped(DEMO));
        run.record(new ImportOutcome.Added(DISK_LOADER, new TakenAuthors(List.of(GRAFOV), List.of(KOVROV))));
        run.advance(new ImportProgress(4, 12, DEMO.page()));
        repository.save(run);
        entityManager.clear();

        ImportRun stored = repository.findById(id).orElseThrow();

        assertThat(stored.getScope()).isEqualTo(run.getScope());
        assertThat(stored.getStartedAt()).isEqualTo(START);
        assertThat(stored.summary()).isEqualTo(new ImportSummary(1, 1, 1, 1));
        assertThat(stored.getAdded()).containsExactly(DISK_LOADER);
        assertThat(stored.getFailed()).containsExactly(G_DOS);
        assertThat(stored.getKnown()).containsExactly(MICRODOS);
        assertThat(stored.getSkipped()).containsExactly(DEMO);
        assertThat(stored.getAuthors()).containsExactly(CREW, GRAFOV);
        assertThat(stored.getPlaces()).containsExactly(KOVROV);
        assertThat(stored.getProgress()).isEqualTo(new ImportProgress(4, 12, DEMO.page()));
    }

    @Test
    void findsTheRunsNotEndedYet() {
        ImportRun going =
                ImportRun.start(new ImportRunId(UUID.randomUUID()), new ImportScope("vektor-06c.ru", null), START);
        ImportRun ended =
                ImportRun.start(new ImportRunId(UUID.randomUUID()), new ImportScope("vektor-06c.ru", null), START);
        ended.end(ImportRunState.DONE, START.plus(1, ChronoUnit.HOURS));
        repository.save(going);
        repository.save(ended);
        entityManager.clear();

        assertThat(repository.findRunning()).extracting(ImportRun::getId).containsExactly(going.getId());
    }
}

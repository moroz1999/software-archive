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
import com.archive.backend.importer.domain.run.ImportLogEntry;
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
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunDownloadedEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunListReadEntry;
import com.archive.backend.importer.read.ImportRunLogLine;
import com.archive.backend.importer.read.ImportRunPlaceEntry;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
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
    private static final Instant AT = Instant.parse("2026-11-14T10:01:00Z");
    private static final Instant LATER = Instant.parse("2026-11-14T10:02:00Z");
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
    private JdbcImportRunReadRepository reports;

    @Autowired
    private EntityManager entityManager;

    @Test
    void keepsARunWithItsCountsAndWhereItIs() {
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        ImportRun run = ImportRun.start(id, new ImportScope("vektor-06c.ru", new VektorSection("zagruzchiki")), START);
        repository.save(run);
        run.record(new ImportOutcome.Failed(G_DOS), AT);
        run.record(new ImportOutcome.Known(MICRODOS, new TakenAuthors(List.of(CREW), List.of())), AT);
        run.record(new ImportOutcome.Skipped(DEMO), AT);
        run.record(new ImportOutcome.Added(DISK_LOADER, new TakenAuthors(List.of(GRAFOV), List.of(KOVROV))), AT);
        run.advance(new ImportProgress(4, 12, DEMO.page()));
        repository.save(run);
        entityManager.clear();

        ImportRun stored = repository.findById(id).orElseThrow();

        assertThat(stored.getScope()).isEqualTo(run.getScope());
        assertThat(stored.getStartedAt()).isEqualTo(START);
        assertThat(stored.summary()).isEqualTo(new ImportSummary(1, 1, 1, 1));
        assertThat(stored.getProgress()).isEqualTo(new ImportProgress(4, 12, DEMO.page()));
        assertThat(stored.getNewLog()).isEmpty();
    }

    @Test
    void appendsEachLineOfTheLogOnceInTheOrderItWasLogged() {
        ImportRunId id = new ImportRunId(UUID.randomUUID());
        ImportRun run = ImportRun.start(id, new ImportScope("vektor-06c.ru", null), START);
        repository.save(run);
        run.log(new ImportLogEntry.ListRead(new ExternalPage("https://vektor-06c.ru/list/"), 1, null, 2), START);
        run.record(new ImportOutcome.Known(MICRODOS, new TakenAuthors(List.of(CREW), List.of())), AT);
        repository.save(run);
        entityManager.clear();
        ImportRun again = repository.findById(id).orElseThrow();
        again.record(new ImportOutcome.Added(DISK_LOADER, new TakenAuthors(List.of(GRAFOV), List.of(KOVROV))), LATER);
        again.record(new ImportOutcome.Failed(G_DOS), LATER);
        again.record(new ImportOutcome.Skipped(DEMO), LATER);
        repository.save(again);
        repository.save(again);

        List<ImportRunLogLine> log = reports.findLatest(null).orElseThrow().log();

        assertThat(log)
                .extracting(ImportRunLogLine::kind)
                .containsExactly("list", "known", "author", "added", "author", "place", "failed", "skipped");
        assertThat(log).extracting(ImportRunLogLine::at).startsWith(START, AT, AT, LATER);
        assertThat(log).extracting(ImportRunLogLine::seq).isSorted();
        assertThat(log.get(0).entry()).isEqualTo(new ImportRunListReadEntry("https://vektor-06c.ru/list/", 1, null, 2));
        assertThat(log.get(1).entry())
                .isEqualTo(new ImportRunKnownEntry(
                        "754", MICRODOS.software().value(), "МикроДОС", false, List.of("credits", "description")));
        assertThat(log.get(2).entry())
                .isEqualTo(new ImportRunAuthorEntry("group", CREW.entity().value(), "Crew", false, false));
        assertThat(log.get(5).entry()).isEqualTo(new ImportRunPlaceEntry("city", "Ковров"));
        assertThat(log.get(6).entry())
                .isEqualTo(new ImportRunFailedEntry("598", "https://vektor-06c.ru/g-dos/", "Timed out"));
        assertThat(log.get(7).entry())
                .isEqualTo(new ImportRunSkippedEntry(
                        "524", "https://vektor-06c.ru/512-demo/", "512 demo", "section", "utility-dlya-pc"));
    }

    @Test
    void readsOnlyTheLinesOfTheLogAfterTheOneGiven() {
        ImportRun run = ImportRun.start(new ImportRunId(UUID.randomUUID()), new ImportScope("caglrc.cc", null), START);
        repository.save(run);
        run.log(new ImportLogEntry.ListRead(new ExternalPage("https://caglrc.cc/list/"), 1, 1, 3), START);
        run.log(
                new ImportLogEntry.Downloaded(
                        "30", "Zone X", ImportLogEntry.DownloadedFile.SCREENSHOT, "zone-x.png", 1234),
                AT);
        repository.save(run);
        long first = reports.findLatest(null).orElseThrow().log().get(0).seq();

        assertThat(reports.findLatest(first).orElseThrow().log())
                .singleElement()
                .satisfies(line -> {
                    assertThat(line.kind()).isEqualTo("download");
                    assertThat(line.entry())
                            .isEqualTo(new ImportRunDownloadedEntry("30", "Zone X", "screenshot", "zone-x.png", 1234));
                });
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

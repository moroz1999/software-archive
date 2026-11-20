package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.run.AddedProgram;
import com.archive.backend.importer.domain.run.FailedProgram;
import com.archive.backend.importer.domain.run.ImportOutcome;
import com.archive.backend.importer.domain.run.ImportRun;
import com.archive.backend.importer.domain.run.ImportRunId;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.ImportScope;
import com.archive.backend.importer.domain.vektor.VektorSection;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        run.record(new ImportOutcome.Known());
        run.record(new ImportOutcome.Added(DISK_LOADER));
        repository.save(run);
        entityManager.clear();

        ImportRun stored = repository.findById(id).orElseThrow();

        assertThat(stored.getScope()).isEqualTo(run.getScope());
        assertThat(stored.getStartedAt()).isEqualTo(START);
        assertThat(stored.summary()).isEqualTo(new ImportSummary(1, 1, 0, 1));
        assertThat(stored.getAdded()).containsExactly(DISK_LOADER);
        assertThat(stored.getFailed()).containsExactly(G_DOS);
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

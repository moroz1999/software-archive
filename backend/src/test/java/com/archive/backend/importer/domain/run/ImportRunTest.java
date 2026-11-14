package com.archive.backend.importer.domain.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.vektor.VektorSection;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImportRunTest {

    private static final ImportRunId ID = new ImportRunId(UUID.randomUUID());
    private static final ImportScope LOADERS = new ImportScope("vektor-06c.ru", new VektorSection("zagruzchiki"));
    private static final Instant START = Instant.parse("2026-11-14T10:00:00Z");
    private static final Instant END = Instant.parse("2026-11-14T10:05:00Z");
    private static final AddedProgram DISK_LOADER =
            new AddedProgram("599", new ImportedEntryId(UUID.randomUUID()), "Disk Loader");
    private static final FailedProgram G_DOS = new FailedProgram(
            "598", new ExternalPage("https://vektor-06c.ru/programmnoe-obespechenie/zagruzchiki/g-dos/"), "Timed out");

    @Test
    void startsRunningWithNothingDone() {
        ImportRun run = ImportRun.start(ID, LOADERS, START);

        assertThat(run.getState()).isEqualTo(ImportRunState.RUNNING);
        assertThat(run.getStartedAt()).isEqualTo(START);
        assertThat(run.getEndedAt()).isNull();
        assertThat(run.summary()).isEqualTo(new ImportSummary(0, 0, 0, 0));
    }

    @Test
    void tellsWhatEachProgramCameTo() {
        ImportRun run = ImportRun.start(ID, LOADERS, START);

        run.record(new ImportOutcome.Added(DISK_LOADER));
        run.record(new ImportOutcome.Known());
        run.record(new ImportOutcome.Known());
        run.record(new ImportOutcome.Skipped());
        run.record(new ImportOutcome.Failed(G_DOS));

        assertThat(run.summary()).isEqualTo(new ImportSummary(1, 2, 1, 1));
        assertThat(run.getAdded()).containsExactly(DISK_LOADER);
        assertThat(run.getFailed()).containsExactly(G_DOS);
    }

    @Test
    void endsDoneOrStoppedAtTheTimeGiven() {
        ImportRun done = ImportRun.start(ID, LOADERS, START);
        ImportRun stopped = ImportRun.start(ID, LOADERS, START);

        done.end(ImportRunState.DONE, END);
        stopped.end(ImportRunState.STOPPED, END);

        assertThat(done.getState()).isEqualTo(ImportRunState.DONE);
        assertThat(stopped.getState()).isEqualTo(ImportRunState.STOPPED);
        assertThat(done.getEndedAt()).isEqualTo(END);
    }

    @Test
    void takesNothingMoreOnceEnded() {
        ImportRun run = ImportRun.start(ID, LOADERS, START);
        run.end(ImportRunState.DONE, END);

        assertThatThrownBy(() -> run.record(new ImportOutcome.Known())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> run.end(ImportRunState.STOPPED, END)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEndRunning() {
        ImportRun run = ImportRun.start(ID, LOADERS, START);

        assertThatThrownBy(() -> run.end(ImportRunState.RUNNING, END)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keepsTheReasonOfAFailureShort() {
        FailedProgram failed = new FailedProgram("1", G_DOS.page(), "x".repeat(5000));

        assertThat(failed.reason()).hasSize(FailedProgram.REASON_MAX_LENGTH);
    }
}

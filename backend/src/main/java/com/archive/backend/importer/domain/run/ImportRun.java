package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ImportSummary;
import com.archive.backend.shared.domain.ddd.AggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * One run of an import and its report: what it imported, when, how far it got, and what each program came to — the
 * programs added and failed one by one, those already taken and left out counted (docs/domain/imports.md).
 */
@Getter
@AggregateRoot
public final class ImportRun {

    private final ImportRunId id;
    private final ImportScope scope;
    private final Instant startedAt;
    private ImportRunState state;
    private @Nullable Instant endedAt;
    private ImportSummary counts;
    private final List<AddedProgram> added;
    private final List<FailedProgram> failed;

    /** A run as it is stored. */
    public ImportRun(
            ImportRunId id,
            ImportScope scope,
            Instant startedAt,
            ImportRunState state,
            @Nullable Instant endedAt,
            ImportSummary counts,
            List<AddedProgram> added,
            List<FailedProgram> failed) {
        this.id = Objects.requireNonNull(id, "id");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.state = Objects.requireNonNull(state, "state");
        this.endedAt = endedAt;
        this.counts = Objects.requireNonNull(counts, "counts");
        this.added = new ArrayList<>(added);
        this.failed = new ArrayList<>(failed);
    }

    public static ImportRun start(ImportRunId id, ImportScope scope, Instant now) {
        return new ImportRun(
                id, scope, now, ImportRunState.RUNNING, null, new ImportSummary(0, 0, 0, 0), List.of(), List.of());
    }

    public List<AddedProgram> getAdded() {
        return List.copyOf(added);
    }

    public List<FailedProgram> getFailed() {
        return List.copyOf(failed);
    }

    /** How many programs came to each outcome so far. */
    public ImportSummary summary() {
        return counts;
    }

    public void record(ImportOutcome outcome) {
        requireRunning();
        ImportSummary c = counts;
        counts = switch (outcome) {
            case ImportOutcome.Added a -> {
                added.add(a.program());
                yield new ImportSummary(c.added() + 1, c.known(), c.skipped(), c.failed());
            }
            case ImportOutcome.Known k -> new ImportSummary(c.added(), c.known() + 1, c.skipped(), c.failed());
            case ImportOutcome.Skipped s -> new ImportSummary(c.added(), c.known(), c.skipped() + 1, c.failed());
            case ImportOutcome.Failed f -> {
                failed.add(f.program());
                yield new ImportSummary(c.added(), c.known(), c.skipped(), c.failed() + 1);
            }
        };
    }

    /** Ends the run: through to the end, or stopped before it by a failure of the whole run or a restart. */
    public void end(ImportRunState end, Instant now) {
        if (end == ImportRunState.RUNNING) {
            throw new IllegalArgumentException("A run ends done or stopped");
        }
        requireRunning();
        state = end;
        endedAt = now;
    }

    private void requireRunning() {
        if (state != ImportRunState.RUNNING) {
            throw new IllegalStateException("The run " + id.value() + " has ended");
        }
    }
}

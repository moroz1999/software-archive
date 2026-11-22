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
 * One run of an import and its report: what it imported, when, where it is, how many programs came to each outcome,
 * and the log of every step it took in the order it went (docs/domain/imports.md). The log only grows: a run read
 * back holds none of it, and keeps the lines logged since, for its repository to append.
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
    private ImportProgress progress;
    private final List<ImportLogLine> newLog = new ArrayList<>();

    /** A run as it is stored. */
    public ImportRun(
            ImportRunId id,
            ImportScope scope,
            Instant startedAt,
            ImportRunState state,
            @Nullable Instant endedAt,
            ImportSummary counts,
            ImportProgress progress) {
        this.id = Objects.requireNonNull(id, "id");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.state = Objects.requireNonNull(state, "state");
        this.endedAt = endedAt;
        this.counts = Objects.requireNonNull(counts, "counts");
        this.progress = Objects.requireNonNull(progress, "progress");
    }

    public static ImportRun start(ImportRunId id, ImportScope scope, Instant now) {
        return new ImportRun(
                id,
                scope,
                now,
                ImportRunState.RUNNING,
                null,
                new ImportSummary(0, 0, 0, 0),
                ImportProgress.READING_LIST);
    }

    /** The lines logged since the run was read, in the order they were logged. */
    public List<ImportLogLine> getNewLog() {
        return List.copyOf(newLog);
    }

    /** Hands the lines logged since over to be stored, once: they are not new any more. */
    public List<ImportLogLine> pullNewLog() {
        List<ImportLogLine> lines = List.copyOf(newLog);
        newLog.clear();
        return lines;
    }

    /** Tells which program of the source the run goes to now. */
    public void advance(ImportProgress next) {
        requireRunning();
        progress = Objects.requireNonNull(next, "next");
    }

    /** Logs a step that is not what a program came to: a page of the list read, a file downloaded. */
    public void log(ImportLogEntry entry, Instant now) {
        requireRunning();
        newLog.add(new ImportLogLine(now, entry));
    }

    /** How many programs came to each outcome so far. */
    public ImportSummary summary() {
        return counts;
    }

    /** Counts and logs what a program came to, and after it the authors and places it took. */
    public void record(ImportOutcome outcome, Instant now) {
        requireRunning();
        ImportSummary c = counts;
        counts = switch (outcome) {
            case ImportOutcome.Added a -> new ImportSummary(c.added() + 1, c.known(), c.skipped(), c.failed());
            case ImportOutcome.Known k -> new ImportSummary(c.added(), c.known() + 1, c.skipped(), c.failed());
            case ImportOutcome.Skipped s -> new ImportSummary(c.added(), c.known(), c.skipped() + 1, c.failed());
            case ImportOutcome.Failed f -> new ImportSummary(c.added(), c.known(), c.skipped(), c.failed() + 1);
        };
        switch (outcome) {
            case ImportOutcome.Added a -> logWithTaken(new ImportLogEntry.Added(a.program()), a.taken(), now);
            case ImportOutcome.Known k -> logWithTaken(new ImportLogEntry.Known(k.program()), k.taken(), now);
            case ImportOutcome.Skipped s -> newLog.add(new ImportLogLine(now, new ImportLogEntry.Skipped(s.program())));
            case ImportOutcome.Failed f -> newLog.add(new ImportLogLine(now, new ImportLogEntry.Failed(f.program())));
        }
    }

    private void logWithTaken(ImportLogEntry outcome, TakenAuthors taken, Instant now) {
        newLog.add(new ImportLogLine(now, outcome));
        taken.authors().forEach(author -> newLog.add(new ImportLogLine(now, new ImportLogEntry.Author(author))));
        taken.places().forEach(place -> newLog.add(new ImportLogLine(now, new ImportLogEntry.Place(place))));
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

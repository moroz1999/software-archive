package com.archive.backend.importer.read;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * A line of the log of a run.
 *
 * @param seq the place of the line among the lines of every run: a later line has a greater one
 * @param at when the step was done; unknown for the lines of runs before the log, carried over from their reports
 * @param kind {@code list}, {@code download}, {@code added}, {@code known}, {@code skipped}, {@code failed},
 *     {@code author} or {@code place}
 */
public record ImportRunLogLine(long seq, @Nullable Instant at, String kind, ImportRunLogEntry entry) {}

package com.archive.backend.importer.web.dto;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * A line of the log of a run: {@code kind} tells what {@code entry} is — {@code list}, {@code download},
 * {@code added}, {@code known}, {@code skipped}, {@code failed}, {@code author} or {@code place}.
 */
public record ImportRunLogLineDto(long seq, @Nullable Instant at, String kind, Object entry) {}

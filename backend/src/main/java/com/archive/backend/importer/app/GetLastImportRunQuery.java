package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.run.ImportLogSeq;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

/**
 * The report of the run of an import started last, with the lines of its log after the one given: a report read again
 * while the run goes on asks only for what it has not seen. All of them without one.
 */
public record GetLastImportRunQuery(Actor actor, @Nullable ImportLogSeq after) {}

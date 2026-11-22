package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.Instant;
import java.util.Objects;

/** A step of a run and when it was done. */
@ValueObject
public record ImportLogLine(Instant at, ImportLogEntry entry) {

    public ImportLogLine {
        Objects.requireNonNull(at, "at");
        Objects.requireNonNull(entry, "entry");
    }
}

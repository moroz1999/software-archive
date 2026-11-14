package com.archive.backend.importer.domain.run;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/** The id of a run of an import. */
@ValueObject
public record ImportRunId(UUID value) {

    public ImportRunId {
        Objects.requireNonNull(value, "value");
    }
}

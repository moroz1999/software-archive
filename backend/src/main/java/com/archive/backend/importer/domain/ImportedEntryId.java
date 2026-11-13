package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * The id of the entry of the archive an import added: software or a label, as its origin tells.
 */
@ValueObject
public record ImportedEntryId(UUID value) {

    public ImportedEntryId {
        Objects.requireNonNull(value, "value");
    }
}

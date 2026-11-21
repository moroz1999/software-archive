package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * An author of the source a run took for the first time: a person, a group or a label it created, or a person or a
 * group of the archive it found to be them; under the name the archive had for them then.
 */
@ValueObject
public record TakenAuthor(ExternalKind kind, ImportedEntryId entity, String name, boolean created) {

    public TakenAuthor {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(name, "name");
    }
}

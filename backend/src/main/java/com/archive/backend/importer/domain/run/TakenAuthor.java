package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;

/**
 * An author of the source a run took for the first time: a person, a group or a label it created, or a person or a
 * group of the archive it found to be them; under the name the archive had for them then.
 */
@ValueObject
public record TakenAuthor(ExternalKind kind, ImportedEntryId entity, String name, boolean created) {

    /** The kinds an author of the source is taken as. */
    public static final List<ExternalKind> KINDS = List.of(ExternalKind.AUTHOR, ExternalKind.GROUP, ExternalKind.LABEL);

    public TakenAuthor {
        Objects.requireNonNull(kind, "kind");
        if (!KINDS.contains(kind)) {
            throw new IllegalArgumentException("An author is taken as a person, a group or a label, not as " + kind);
        }
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(name, "name");
    }
}

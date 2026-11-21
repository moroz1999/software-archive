package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.software.domain.SoftwareSupplement;
import java.util.Objects;
import java.util.Set;

/** A program a run found in the archive: its id in the source, the software it is, its title then and what was added to it. */
@ValueObject
public record KnownProgram(
        String externalId, ImportedEntryId software, String title, Set<SoftwareSupplement> supplements) {

    public KnownProgram {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(software, "software");
        Objects.requireNonNull(title, "title");
        supplements = Set.copyOf(supplements);
    }
}

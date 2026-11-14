package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A program a run added: its id in the source archive, the software it became and its title then. */
@ValueObject
public record AddedProgram(String externalId, ImportedEntryId software, String title) {

    public AddedProgram {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(software, "software");
        Objects.requireNonNull(title, "title");
    }
}

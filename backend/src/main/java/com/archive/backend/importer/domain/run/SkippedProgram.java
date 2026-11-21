package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A program a run left out by the rules of the import: its id, page and title in the source, why, and the name it was left out by. */
@ValueObject
public record SkippedProgram(String externalId, ExternalPage page, String title, SkipReason reason, String detail) {

    public SkippedProgram {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(detail, "detail");
    }
}

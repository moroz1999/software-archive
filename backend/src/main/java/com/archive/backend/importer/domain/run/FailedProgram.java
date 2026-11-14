package com.archive.backend.importer.domain.run;

import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A program a run could not add: its id and page in the source archive, and why, cut to a readable length. */
@ValueObject
public record FailedProgram(String externalId, ExternalPage page, String reason) {

    public static final int REASON_MAX_LENGTH = 1000;

    public FailedProgram {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(reason, "reason");
        reason = reason.length() > REASON_MAX_LENGTH ? reason.substring(0, REASON_MAX_LENGTH) : reason;
    }
}

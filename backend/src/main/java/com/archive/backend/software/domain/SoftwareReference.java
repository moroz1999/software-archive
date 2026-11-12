package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** One person, group or exact label chosen for software. */
@ValueObject
public record SoftwareReference(SoftwareReferenceType type, SoftwareReferenceId id) {

    public SoftwareReference {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }
}

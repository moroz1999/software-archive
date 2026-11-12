package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/** The id of a person, group or label referenced by software. */
@ValueObject
public record SoftwareReferenceId(UUID value) {

    public SoftwareReferenceId {
        Objects.requireNonNull(value, "value");
    }
}

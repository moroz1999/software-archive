package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record ReleaseId(UUID value) {

    public ReleaseId {
        Objects.requireNonNull(value, "value");
    }
}

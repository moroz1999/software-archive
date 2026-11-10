package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

@ValueObject
public record ReleaseDescription(String value) {

    public ReleaseDescription {
        Objects.requireNonNull(value, "value");
    }
}

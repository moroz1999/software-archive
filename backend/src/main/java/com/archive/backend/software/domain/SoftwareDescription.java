package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

@ValueObject
public record SoftwareDescription(String value) {

    public SoftwareDescription {
        Objects.requireNonNull(value, "value");
    }
}

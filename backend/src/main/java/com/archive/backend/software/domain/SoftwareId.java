package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record SoftwareId(UUID value) {

    public SoftwareId {
        Objects.requireNonNull(value, "value");
    }
}

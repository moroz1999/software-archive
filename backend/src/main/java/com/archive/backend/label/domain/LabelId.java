package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record LabelId(UUID value) {

    public LabelId {
        Objects.requireNonNull(value, "value");
    }
}

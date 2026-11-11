package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

@ValueObject
public record GroupNotes(String value) {

    public GroupNotes {
        Objects.requireNonNull(value, "value");
    }
}

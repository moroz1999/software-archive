package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The normalized text entered in a software relation picker; it may be empty. */
@ValueObject
public record SoftwareReferencePhrase(String value) {

    public SoftwareReferencePhrase {
        value = Objects.requireNonNull(value, "value").strip();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }
}

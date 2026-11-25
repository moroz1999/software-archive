package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** How the software is loaded, run and played, kept apart from its description (docs/domain/software.md). */
@ValueObject
public record SoftwareInstructions(String value) {

    public SoftwareInstructions {
        Objects.requireNonNull(value, "value");
    }
}

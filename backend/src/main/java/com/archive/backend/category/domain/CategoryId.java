package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * The id of a category of software.
 */
@ValueObject
public record CategoryId(UUID value) {

    public CategoryId {
        Objects.requireNonNull(value, "value");
    }
}

package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * The code of a group type in the reference book, such as demogroup.
 */
@ValueObject
public record GroupTypeCode(String value) {

    public GroupTypeCode {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidValueException("group.type-code-blank", "A group type must not be blank.");
        }
    }
}

package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * The name of the person behind the labels.
 */
@ValueObject
public record RealName(String value) {

    public static final int MAX_LENGTH = 255;

    public RealName {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidValueException("author.real-name-blank", "A real name must not be blank.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "author.real-name-too-long", "A real name must be at most " + MAX_LENGTH + " characters long.");
        }
    }
}

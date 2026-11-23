package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/** The name of an item of hardware or of a group of it in one language: not blank, at most 255 characters. */
@ValueObject
public record HardwareName(String value) {

    private static final int MAX_LENGTH = 255;

    public HardwareName {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Hardware needs a name.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("hardware.name-too-long", "A name is at most 255 characters long.");
        }
    }
}

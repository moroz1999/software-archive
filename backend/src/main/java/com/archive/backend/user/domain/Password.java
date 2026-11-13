package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * A password as the user typed it; it never leaves the request that carries it, and is never printed.
 */
@ValueObject
public record Password(String value) {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 128;

    public Password {
        Objects.requireNonNull(value, "value");
        if (value.length() < MIN_LENGTH) {
            throw new InvalidValueException(
                    "user.password-too-short", "A password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "user.password-too-long", "A password must be at most " + MAX_LENGTH + " characters long.");
        }
    }

    @Override
    public String toString() {
        return "Password[***]";
    }
}

package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * The name a user is known by in the archive; one account per name, whatever its letter case.
 */
@ValueObject
public record UserName(String value) {

    public static final int MAX_LENGTH = 64;

    public UserName {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidValueException("user.name-required", "A user name must not be blank.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "user.name-too-long", "A user name must be at most " + MAX_LENGTH + " characters long.");
        }
    }
}

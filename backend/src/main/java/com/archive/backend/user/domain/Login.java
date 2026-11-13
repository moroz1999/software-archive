package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * What a user signs in with: their user name or their email.
 */
@ValueObject
public record Login(String value) {

    public Login {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new InvalidValueException("user.login-required", "A login must not be blank.");
        }
    }
}

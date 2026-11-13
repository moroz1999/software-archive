package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * What is kept of a password.
 */
@ValueObject
public record PasswordHash(String value) {

    public PasswordHash {
        Objects.requireNonNull(value, "value");
    }

    @Override
    public String toString() {
        return "PasswordHash[***]";
    }
}

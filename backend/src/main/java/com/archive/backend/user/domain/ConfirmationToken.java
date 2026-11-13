package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * The secret of a confirmation link. Only its digest is kept: the secret itself goes to the email only.
 */
@ValueObject
public record ConfirmationToken(String value) {

    public ConfirmationToken {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidValueException("user.confirmation-token-required", "A confirmation link needs its token.");
        }
    }

    public static ConfirmationToken generate() {
        return new ConfirmationToken(TokenDigest.randomSecret());
    }

    public TokenDigest digest() {
        return TokenDigest.of(value);
    }

    @Override
    public String toString() {
        return "ConfirmationToken[***]";
    }
}

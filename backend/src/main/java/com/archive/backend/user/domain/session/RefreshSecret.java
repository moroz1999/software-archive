package com.archive.backend.user.domain.session;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.TokenDigest;
import java.util.Objects;

/**
 * The secret that keeps a session going. Only its digest is kept; the secret itself lives in the browser.
 */
@ValueObject
public record RefreshSecret(String value) {

    public RefreshSecret {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidValueException("auth.refresh-secret-required", "A session needs its secret.");
        }
    }

    public static RefreshSecret generate() {
        return new RefreshSecret(TokenDigest.randomSecret());
    }

    public TokenDigest digest() {
        return TokenDigest.of(value);
    }

    @Override
    public String toString() {
        return "RefreshSecret[***]";
    }
}

package com.archive.backend.user.domain.session;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.Instant;

/**
 * A short-lived signed proof of who the user is and which groups they are in.
 */
@ValueObject
public record AccessToken(String value, Instant expiresAt) {

    @Override
    public String toString() {
        return "AccessToken[***]";
    }
}

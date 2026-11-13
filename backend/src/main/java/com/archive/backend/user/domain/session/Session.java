package com.archive.backend.user.domain.session;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.user.domain.TokenDigest;
import com.archive.backend.user.domain.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * A signed-in session of a user, known by the digest of its refresh secret.
 */
@AggregateRoot
@Getter
public final class Session {

    /**
     * How long a session lasts without being refreshed.
     */
    public static final Duration LIFETIME = Duration.ofDays(30);

    private final TokenDigest digest;
    private final UserId userId;
    private final Instant expiresAt;
    private final RememberMe remember;

    public Session(TokenDigest digest, UserId userId, Instant expiresAt, RememberMe remember) {
        this.digest = Objects.requireNonNull(digest, "digest");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.remember = Objects.requireNonNull(remember, "remember");
    }

    public static Session open(RefreshSecret secret, UserId userId, RememberMe remember, Instant now) {
        return new Session(secret.digest(), userId, now.plus(LIFETIME), remember);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}

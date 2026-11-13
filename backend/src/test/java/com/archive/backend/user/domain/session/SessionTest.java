package com.archive.backend.user.domain.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final UserId USER = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Test
    void aSessionIsKnownByTheDigestOfItsSecretAndLasts30Days() {
        RefreshSecret secret = new RefreshSecret("secret");

        Session session = Session.open(secret, USER, new RememberMe(true), NOW);

        assertThat(session.getDigest()).isEqualTo(secret.digest());
        assertThat(session.getUserId()).isEqualTo(USER);
        assertThat(session.getRemember()).isEqualTo(new RememberMe(true));
        assertThat(session.isExpired(NOW.plus(Duration.ofDays(30)).minusSeconds(1)))
                .isFalse();
        assertThat(session.isExpired(NOW.plus(Duration.ofDays(30)))).isTrue();
    }

    @Test
    void generatedSecretsAreUrlSafeAndUnique() {
        RefreshSecret first = RefreshSecret.generate();

        assertThat(first.value()).matches("[A-Za-z0-9_-]{43,}");
        assertThat(RefreshSecret.generate()).isNotEqualTo(first);
        assertThat(first.digest().value()).matches("[0-9a-f]{64}");
    }

    @Test
    void aSecretIsRequiredAndNeverPrinted() {
        assertThatThrownBy(() -> new RefreshSecret(" "))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("auth.refresh-secret-required"));
        assertThat(new RefreshSecret("secret")).hasToString("RefreshSecret[***]");
    }
}

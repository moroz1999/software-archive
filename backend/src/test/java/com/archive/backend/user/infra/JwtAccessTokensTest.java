package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.shared.config.AuthProperties;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.session.AccessToken;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAccessTokensTest {

    private static final AuthProperties PROPERTIES = new AuthProperties(
            "a-test-secret-of-at-least-32-bytes-long",
            Duration.ofMinutes(15),
            "http://localhost:5173",
            "no-reply@localhost",
            true);
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void signsWhoTheUserIsAndTheirGroups() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        ConfirmationToken token = new ConfirmationToken("token");
        User user = User.register(
                new UserId(ID),
                new UserName("Zed"),
                new Email("zed@example.com"),
                new PasswordHash("hash"),
                token,
                now,
                Language.EN);
        user.confirm(token, now);
        user.grant(UserGroup.ADMIN);

        AccessToken issued = new JwtAccessTokens(PROPERTIES).issue(user, now);

        Jwt jwt = new SecurityConfig().jwtDecoder(PROPERTIES).decode(issued.value());
        assertThat(jwt.getSubject()).isEqualTo(ID.toString());
        assertThat(jwt.getClaimAsStringList("groups")).containsExactlyInAnyOrder("admin", "member");
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
        assertThat(issued.expiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
    }
}

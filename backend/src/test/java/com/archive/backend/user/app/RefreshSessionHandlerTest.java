package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.ACCESS_TOKENS;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.app.UserFixtures.MovingClock;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.SessionExpiredException;
import com.archive.backend.user.domain.session.Sessions;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RefreshSessionHandlerTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    private final Sessions sessions = new Sessions(sessionRepository, users, ACCESS_TOKENS);
    private final MovingClock clock = new MovingClock();
    private final RefreshSessionHandler handler = new RefreshSessionHandler(sessions, clock);

    @Test
    void replacesTheSecretWithANewOne() {
        IssuedSession opened = open(new RememberMe(false));
        clock.advance(Duration.ofDays(29));

        IssuedSession refreshed = handler.handle(new RefreshSessionCommand(opened.refresh()));

        assertThat(refreshed.refresh()).isNotEqualTo(opened.refresh());
        assertThat(refreshed.remember()).isEqualTo(new RememberMe(false));
        assertThat(refreshed.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(59)));
        assertThat(refreshed.access().expiresAt())
                .isEqualTo(NOW.plus(Duration.ofDays(29)).plus(Duration.ofMinutes(15)));
        assertThat(sessionRepository.count()).isEqualTo(1);
        assertExpired(opened.refresh());
    }

    @Test
    void theAccessTokenTellsTheGroupsTheUserHasNow() {
        IssuedSession opened = open(new RememberMe(true));
        User user = users.findById(id(1)).orElseThrow();
        user.grant(UserGroup.MODERATOR);
        users.save(user);

        IssuedSession refreshed = handler.handle(new RefreshSessionCommand(opened.refresh()));

        assertThat(refreshed.access().value()).isEqualTo("access:" + id(1).value() + ":member,moderator");
    }

    @Test
    void anExpiredSessionEnds() {
        IssuedSession opened = open(new RememberMe(true));
        clock.advance(Duration.ofDays(30));

        assertExpired(opened.refresh());
        assertThat(sessionRepository.count()).isZero();
    }

    @Test
    void anUnknownSecretIsNoSession() {
        assertExpired(new RefreshSecret("unknown"));
    }

    @Test
    void aSessionOfADeletedAccountEnds() {
        IssuedSession opened = open(new RememberMe(true));
        users.delete(users.findById(id(1)).orElseThrow());

        assertExpired(opened.refresh());
    }

    private IssuedSession open(RememberMe remember) {
        users.save(UserFixtures.confirmed(1, "Zed"));
        return sessions.open(users.findById(id(1)).orElseThrow(), remember, NOW);
    }

    private void assertExpired(RefreshSecret secret) {
        assertThatThrownBy(() -> handler.handle(new RefreshSessionCommand(secret)))
                .isInstanceOfSatisfying(
                        SessionExpiredException.class, e -> assertThat(e.code()).isEqualTo("auth.session-expired"));
    }
}

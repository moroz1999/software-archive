package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.ACCESS_TOKENS;
import static com.archive.backend.user.app.UserFixtures.HASHER;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.PASSWORD;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.app.UserFixtures.MovingClock;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.Session;
import com.archive.backend.user.domain.session.Sessions;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class LogInHandlerTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    private final MovingClock clock = new MovingClock();
    private final LogInHandler handler =
            new LogInHandler(users, HASHER, new Sessions(sessionRepository, users, ACCESS_TOKENS), clock);

    @Test
    void signsInByUserNameWhateverItsCase() {
        users.save(UserFixtures.confirmed(1, "Zed", UserGroup.ADMIN));

        IssuedSession session = handler.handle(new LogInCommand(new Login("zED"), PASSWORD, new RememberMe(true)));

        assertThat(session.access().value()).isEqualTo("access:" + id(1).value() + ":admin,member");
        assertThat(session.access().expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
        assertThat(session.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
        Session stored =
                sessionRepository.findByDigest(session.refresh().digest()).orElseThrow();
        assertThat(stored.getUserId()).isEqualTo(id(1));
        assertThat(stored.getRemember()).isEqualTo(new RememberMe(true));
    }

    @Test
    void signsInByEmailAndRemembersTheChoice() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        IssuedSession session =
                handler.handle(new LogInCommand(new Login("Zed@Example.com"), PASSWORD, new RememberMe(false)));

        assertThat(session.remember()).isEqualTo(new RememberMe(false));
        assertThat(sessionRepository
                        .findByDigest(session.refresh().digest())
                        .orElseThrow()
                        .getRemember())
                .isEqualTo(new RememberMe(false));
    }

    @Test
    void refusesAWrongPassword() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        assertThatThrownBy(() -> handler.handle(
                        new LogInCommand(new Login("Zed"), new Password("password2"), new RememberMe(true))))
                .isInstanceOfSatisfying(
                        com.archive.backend.user.domain.CredentialsInvalidException.class,
                        e -> assertThat(e.code()).isEqualTo("user.credentials-invalid"));
        assertThat(sessionRepository.count()).isZero();
    }

    @Test
    void refusesAnUnknownLoginTheSameWay() {
        assertThatThrownBy(() -> handler.handle(new LogInCommand(new Login("Nobody"), PASSWORD, new RememberMe(true))))
                .isInstanceOf(com.archive.backend.user.domain.CredentialsInvalidException.class);
    }

    @Test
    void refusesAnUnconfirmedAccount() {
        users.save(UserFixtures.unconfirmed(1, "Zed", new ConfirmationToken("t"), NOW));

        assertThatThrownBy(() -> handler.handle(new LogInCommand(new Login("Zed"), PASSWORD, new RememberMe(true))))
                .isInstanceOfSatisfying(
                        com.archive.backend.user.domain.UserUnconfirmedException.class,
                        e -> assertThat(e.code()).isEqualTo("user.unconfirmed"));
        assertThat(sessionRepository.count()).isZero();
    }
}

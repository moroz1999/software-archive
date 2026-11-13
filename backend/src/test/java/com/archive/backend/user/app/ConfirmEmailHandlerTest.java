package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.ACCESS_TOKENS;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.app.UserFixtures.MovingClock;
import com.archive.backend.user.domain.AccountConfirmation;
import com.archive.backend.user.domain.ConfirmationExpiredException;
import com.archive.backend.user.domain.ConfirmationInvalidException;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserStatus;
import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.Sessions;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ConfirmEmailHandlerTest {

    private static final ConfirmationToken TOKEN = new ConfirmationToken("token");

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    private final MovingClock clock = new MovingClock();
    private final ConfirmEmailHandler handler = new ConfirmEmailHandler(
            users, new AccountConfirmation(users), new Sessions(sessionRepository, users, ACCESS_TOKENS), clock);

    @Test
    void confirmsTheAccountAndSignsItsUserIn() {
        users.save(UserFixtures.confirmed(1, "Admin", UserGroup.ADMIN));
        users.save(UserFixtures.unconfirmed(2, "Zed", TOKEN, NOW));

        IssuedSession session = handler.handle(new ConfirmEmailCommand(TOKEN));

        User user = users.findById(id(2)).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.CONFIRMED);
        assertThat(user.getGroups()).containsExactly(UserGroup.MEMBER);
        assertThat(session.access().value()).isEqualTo("access:" + id(2).value() + ":member");
        assertThat(session.remember()).isEqualTo(new RememberMe(true));
        assertThat(sessionRepository.findByDigest(session.refresh().digest()))
                .hasValueSatisfying(stored -> assertThat(stored.getUserId()).isEqualTo(id(2)));
    }

    @Test
    void theFirstConfirmedAccountBecomesAnAdministrator() {
        users.save(UserFixtures.unconfirmed(1, "Zed", TOKEN, NOW));

        handler.handle(new ConfirmEmailCommand(TOKEN));

        assertThat(users.findById(id(1)).orElseThrow().getGroups())
                .containsExactlyInAnyOrder(UserGroup.ADMIN, UserGroup.MEMBER);
    }

    @Test
    void refusesAnUnknownLink() {
        users.save(UserFixtures.unconfirmed(1, "Zed", TOKEN, NOW));

        assertThatThrownBy(() -> handler.handle(new ConfirmEmailCommand(new ConfirmationToken("other"))))
                .isInstanceOfSatisfying(
                        ConfirmationInvalidException.class,
                        e -> assertThat(e.code()).isEqualTo("user.confirmation-invalid"));
        assertThat(sessionRepository.count()).isZero();
    }

    @Test
    void refusesAnExpiredLink() {
        users.save(UserFixtures.unconfirmed(1, "Zed", TOKEN, NOW));
        clock.advance(Duration.ofDays(7));

        assertThatThrownBy(() -> handler.handle(new ConfirmEmailCommand(TOKEN)))
                .isInstanceOfSatisfying(
                        ConfirmationExpiredException.class,
                        e -> assertThat(e.code()).isEqualTo("user.confirmation-expired"));
        assertThat(users.findById(id(1)).orElseThrow().getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
        assertThat(sessionRepository.count()).isZero();
    }

    @Test
    void aLinkWorksOnce() {
        users.save(UserFixtures.unconfirmed(1, "Zed", TOKEN, NOW));
        handler.handle(new ConfirmEmailCommand(TOKEN));

        assertThatThrownBy(() -> handler.handle(new ConfirmEmailCommand(TOKEN)))
                .isInstanceOf(ConfirmationInvalidException.class);
    }
}

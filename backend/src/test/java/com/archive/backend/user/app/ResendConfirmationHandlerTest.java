package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.HASHER;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.PASSWORD;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.app.UserFixtures.MovingClock;
import com.archive.backend.user.app.UserFixtures.RecordingMailer;
import com.archive.backend.user.domain.ConfirmationInvalidException;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.CredentialsInvalidException;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.Password;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserAlreadyConfirmedException;
import com.archive.backend.user.domain.UserStatus;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ResendConfirmationHandlerTest {

    private static final ConfirmationToken OLD = new ConfirmationToken("old");

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final RecordingMailer mailer = new RecordingMailer();
    private final MovingClock clock = new MovingClock();
    private final ResendConfirmationHandler handler =
            new ResendConfirmationHandler(users, HASHER, UserFixtures.mailing(mailer), clock);

    @Test
    void mailsANewLinkAndTheOldOneStopsWorking() {
        users.save(UserFixtures.unconfirmed(1, "Zed", OLD, NOW.minus(Duration.ofDays(10))));

        handler.handle(new ResendConfirmationCommand(new Login("zed@example.com"), PASSWORD, Language.EN));

        assertThat(mailer.letters).singleElement().satisfies(letter -> {
            assertThat(letter.to()).isEqualTo(new Email("zed@example.com"));
            assertThat(letter.language()).isEqualTo(Language.EN);
            User user = users.findById(id(1)).orElseThrow();
            assertThatThrownBy(() -> user.confirm(OLD, NOW)).isInstanceOf(ConfirmationInvalidException.class);
            user.confirm(letter.token(), NOW.plus(Duration.ofDays(6)));
            assertThat(user.getStatus()).isEqualTo(UserStatus.CONFIRMED);
        });
    }

    @Test
    void refusesAWrongPassword() {
        users.save(UserFixtures.unconfirmed(1, "Zed", OLD, NOW));

        assertThatThrownBy(() -> handler.handle(
                        new ResendConfirmationCommand(new Login("Zed"), new Password("password2"), Language.EN)))
                .isInstanceOf(CredentialsInvalidException.class);
        assertThat(mailer.letters).isEmpty();
    }

    @Test
    void refusesAnUnknownLogin() {
        assertThatThrownBy(
                        () -> handler.handle(new ResendConfirmationCommand(new Login("Nobody"), PASSWORD, Language.EN)))
                .isInstanceOf(CredentialsInvalidException.class);
    }

    @Test
    void aConfirmedAccountNeedsNoLink() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        assertThatThrownBy(() -> handler.handle(new ResendConfirmationCommand(new Login("Zed"), PASSWORD, Language.EN)))
                .isInstanceOfSatisfying(
                        UserAlreadyConfirmedException.class,
                        e -> assertThat(e.code()).isEqualTo("user.already-confirmed"));
        assertThat(mailer.letters).isEmpty();
    }
}

package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.HASHER;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.app.UserFixtures.MovingClock;
import com.archive.backend.user.app.UserFixtures.RecordingMailer;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.Registrations;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserStatus;
import java.time.Duration;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class RegisterUserHandlerTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final RecordingMailer mailer = new RecordingMailer();
    private final MovingClock clock = new MovingClock();
    private final RegisterUserHandler handler =
            new RegisterUserHandler(users, new Registrations(users, HASHER), UserFixtures.mailing(mailer), clock);

    @Test
    void storesAnUnconfirmedAccountAndMailsItsLink() {
        handler.handle(command("Zed", "zed@example.com"));

        User user = users.findByName(new UserName("Zed")).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(new Email("zed@example.com"));
        assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("hashed:password1"));
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
        assertThat(mailer.letters).singleElement().satisfies(letter -> {
            assertThat(letter.to()).isEqualTo(new Email("zed@example.com"));
            assertThat(letter.name()).isEqualTo(new UserName("Zed"));
            assertThat(letter.language()).isEqualTo(InterfaceLanguage.RU);
            assertThat(Objects.requireNonNull(user.getConfirmation()).digest())
                    .isEqualTo(letter.token().digest());
        });
    }

    @Test
    void refusesATakenUserNameWhateverItsCase() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        assertTaken(() -> handler.handle(command("ZED", "other@example.com")), "user.name-taken", "name");
    }

    @Test
    void refusesATakenEmail() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        assertTaken(() -> handler.handle(command("Other", "ZED@example.com")), "user.email-taken", "email");
    }

    @Test
    void anAccountWaitingForItsLinkKeepsItsNameAndEmail() {
        users.save(UserFixtures.unconfirmed(1, "Zed", new ConfirmationToken("t"), NOW.minus(Duration.ofDays(6))));

        assertTaken(() -> handler.handle(command("Zed", "new@example.com")), "user.name-taken", "name");
        assertTaken(() -> handler.handle(command("New", "zed@example.com")), "user.email-taken", "email");
    }

    @Test
    void anAbandonedAccountGivesUpItsNameAndEmail() {
        users.save(UserFixtures.unconfirmed(1, "Zed", new ConfirmationToken("t"), NOW.minus(Duration.ofDays(8))));
        users.save(UserFixtures.unconfirmed(2, "Axe", new ConfirmationToken("u"), NOW.minus(Duration.ofDays(8))));

        handler.handle(command("Zed", "axe@example.com"));

        assertThat(users.count()).isEqualTo(1);
        User user = users.findByName(new UserName("Zed")).orElseThrow();
        assertThat(user.getId()).isNotEqualTo(UserFixtures.id(1));
        assertThat(user.getEmail()).isEqualTo(new Email("axe@example.com"));
    }

    private void assertTaken(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, String code, String field) {
        int before = users.count();
        assertThatThrownBy(call).isInstanceOfSatisfying(RuleViolationException.class, e -> {
            assertThat(e.code()).isEqualTo(code);
            assertThat(e.field()).isEqualTo(field);
        });
        assertThat(users.count()).isEqualTo(before);
        assertThat(mailer.letters).isEmpty();
    }

    private static RegisterUserCommand command(String name, String email) {
        return new RegisterUserCommand(new UserName(name), new Email(email), PASSWORD, InterfaceLanguage.RU);
    }
}

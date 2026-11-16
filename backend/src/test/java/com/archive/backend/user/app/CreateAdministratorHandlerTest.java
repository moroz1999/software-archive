package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.HASHER;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.app.UserFixtures.MovingClock;
import com.archive.backend.user.domain.AdministratorExistsException;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Registrations;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserStatus;
import java.time.Duration;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class CreateAdministratorHandlerTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final CreateAdministratorHandler handler =
            new CreateAdministratorHandler(users, new Registrations(users, HASHER), new MovingClock());

    @Test
    void storesAConfirmedAdministratorWhoSignsInAtOnce() {
        handler.handle(command("Zed", "zed@example.com"));

        User user = users.findByName(new UserName("Zed")).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(new Email("zed@example.com"));
        assertThat(user.getStatus()).isEqualTo(UserStatus.CONFIRMED);
        assertThat(user.getGroups()).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN);
        assertThat(user.getConfirmation()).isNull();
        assertThat(user.getRegisteredAt()).isEqualTo(NOW);
        assertThat(user.pullEvents()).isEmpty();
        assertThatCode(() -> user.authenticate(PASSWORD, HASHER)).doesNotThrowAnyException();
    }

    @Test
    void refusesOnceTheArchiveHasAnAdministrator() {
        users.save(UserFixtures.confirmed(1, "Boss", UserGroup.ADMIN));

        assertRefused(() -> handler.handle(command("Zed", "zed@example.com")), "user.admin-exists", null);
        assertThatThrownBy(() -> handler.handle(command("Zed", "zed@example.com")))
                .isInstanceOf(AdministratorExistsException.class);
    }

    @Test
    void refusesATakenUserName() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        assertRefused(() -> handler.handle(command("ZED", "other@example.com")), "user.name-taken", "name");
    }

    @Test
    void refusesATakenEmail() {
        users.save(UserFixtures.confirmed(1, "Zed"));

        assertRefused(() -> handler.handle(command("Other", "ZED@example.com")), "user.email-taken", "email");
    }

    @Test
    void anAbandonedAccountGivesUpItsNameAndEmail() {
        users.save(UserFixtures.unconfirmed(1, "Zed", new ConfirmationToken("t"), NOW.minus(Duration.ofDays(8))));

        handler.handle(command("Zed", "zed@example.com"));

        assertThat(users.count()).isEqualTo(1);
        User user = users.findByName(new UserName("Zed")).orElseThrow();
        assertThat(user.getId()).isNotEqualTo(UserFixtures.id(1));
        assertThat(user.getGroups()).contains(UserGroup.ADMIN);
    }

    private void assertRefused(ThrowingCallable call, String code, @Nullable String field) {
        int before = users.count();
        assertThatThrownBy(call).isInstanceOfSatisfying(RuleViolationException.class, e -> {
            assertThat(e.code()).isEqualTo(code);
            assertThat(e.field()).isEqualTo(field);
        });
        assertThat(users.count()).isEqualTo(before);
    }

    private static CreateAdministratorCommand command(String name, String email) {
        return new CreateAdministratorCommand(new UserName(name), new Email(email), PASSWORD);
    }
}

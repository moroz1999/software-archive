package com.archive.backend.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final ConfirmationToken TOKEN = new ConfirmationToken("token");
    private static final PasswordHasher HASHER = new PasswordHasher() {
        @Override
        public PasswordHash hash(Password password) {
            return new PasswordHash("hashed:" + password.value());
        }

        @Override
        public boolean matches(Password password, PasswordHash hash) {
            return hash.value().equals("hashed:" + password.value());
        }
    };

    @Test
    void aNewAccountIsUnconfirmedAndInNoGroup() {
        User user = registered();

        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
        assertThat(user.getGroups()).isEmpty();
        assertThat(user.getRegisteredAt()).isEqualTo(NOW);
        assertThat(user.getConfirmation())
                .isEqualTo(new PendingConfirmation(TOKEN.digest(), NOW.plus(Duration.ofDays(7))));
    }

    @Test
    void aNewAccountGetsItsLinkMailed() {
        assertThat(registered().pullEvents())
                .containsExactly(new ConfirmationLinkIssued(
                        new Email("zed@example.com"), new UserName("Zed"), TOKEN, InterfaceLanguage.EN));
    }

    @Test
    void confirmingMakesTheUserAMember() {
        User user = registered();

        user.confirm(TOKEN, NOW.plus(Duration.ofDays(7)).minusSeconds(1));

        assertThat(user.getStatus()).isEqualTo(UserStatus.CONFIRMED);
        assertThat(user.getGroups()).containsExactly(UserGroup.MEMBER);
        assertThat(user.getConfirmation()).isNull();
    }

    @Test
    void aLinkStopsWorkingAfterAWeek() {
        User user = registered();

        assertThatThrownBy(() -> user.confirm(TOKEN, NOW.plus(Duration.ofDays(7))))
                .isInstanceOf(ConfirmationExpiredException.class);
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
    }

    @Test
    void anotherSecretDoesNotConfirm() {
        User user = registered();

        assertThatThrownBy(() -> user.confirm(new ConfirmationToken("other"), NOW))
                .isInstanceOf(ConfirmationInvalidException.class);
    }

    @Test
    void aLinkWorksOnce() {
        User user = registered();
        user.confirm(TOKEN, NOW);

        assertThatThrownBy(() -> user.confirm(TOKEN, NOW)).isInstanceOf(ConfirmationInvalidException.class);
    }

    @Test
    void aNewLinkReplacesTheOldOne() {
        User user = registered();
        ConfirmationToken renewed = new ConfirmationToken("renewed");
        Instant later = NOW.plus(Duration.ofDays(10));

        user.renewConfirmation(renewed, later, InterfaceLanguage.EN);

        assertThatThrownBy(() -> user.confirm(TOKEN, later)).isInstanceOf(ConfirmationInvalidException.class);
        user.confirm(renewed, later.plus(Duration.ofDays(6)));
        assertThat(user.getStatus()).isEqualTo(UserStatus.CONFIRMED);
    }

    @Test
    void aConfirmedAccountGetsNoNewLink() {
        User user = confirmed();

        assertThatThrownBy(() -> user.renewConfirmation(TOKEN, NOW, InterfaceLanguage.EN))
                .isInstanceOf(UserAlreadyConfirmedException.class);
    }

    @Test
    void groupsAreGranted() {
        User user = confirmed();

        user.grant(UserGroup.ADMIN);

        assertThat(user.getGroups()).containsExactlyInAnyOrder(UserGroup.ADMIN, UserGroup.MEMBER);
        assertThat(user.actor()).isEqualTo(new Actor(user.getId(), Set.of(UserGroup.ADMIN, UserGroup.MEMBER)));
    }

    @Test
    void anAdministratorChoosesTheGroupsOfAUserWhoStaysAMember() {
        User user = confirmed();
        user.grant(UserGroup.ADMIN);

        user.changeGroups(new GrantedGroups(Set.of(UserGroup.MODERATOR)));

        assertThat(user.getGroups()).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.MODERATOR);
    }

    @Test
    void noGroupsChosenLeaveAMemberOnly() {
        User user = confirmed();
        user.grant(UserGroup.MODERATOR);

        user.changeGroups(new GrantedGroups(Set.of()));

        assertThat(user.getGroups()).containsExactly(UserGroup.MEMBER);
    }

    @Test
    void anUnconfirmedAccountGetsNoGroups() {
        User user = registered();

        assertThatThrownBy(() -> user.changeGroups(new GrantedGroups(Set.of(UserGroup.MODERATOR))))
                .isInstanceOf(UnconfirmedUserGroupsException.class)
                .hasFieldOrPropertyWithValue("code", "user.unconfirmed-groups");
        assertThat(user.getGroups()).isEmpty();
    }

    @Test
    void anUnconfirmedAccountKeepsNoGroupsWhenNoneAreChosen() {
        User user = registered();

        user.changeGroups(new GrantedGroups(Set.of()));

        assertThat(user.getGroups()).isEmpty();
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
    }

    @Test
    void anAccountGetsANewNameAndEmail() {
        User user = confirmed();

        user.rename(new UserName("Axe"), new Email("axe@example.com"));

        assertThat(user.getName()).isEqualTo(new UserName("Axe"));
        assertThat(user.getEmail()).isEqualTo(new Email("axe@example.com"));
    }

    @Test
    void anUnconfirmedAccountRenamedKeepsItsLink() {
        User user = registered();

        user.rename(new UserName("Axe"), new Email("axe@example.com"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
        assertThat(user.getConfirmation())
                .isEqualTo(new PendingConfirmation(TOKEN.digest(), NOW.plus(Duration.ofDays(7))));
    }

    @Test
    void aConfirmedUserSignsInWithTheirPassword() {
        assertThatCode(() -> confirmed().authenticate(new Password("password1"), HASHER))
                .doesNotThrowAnyException();
    }

    @Test
    void aWrongPasswordDoesNotSignIn() {
        assertThatThrownBy(() -> confirmed().authenticate(new Password("password2"), HASHER))
                .isInstanceOf(CredentialsInvalidException.class);
    }

    @Test
    void anUnconfirmedUserDoesNotSignIn() {
        assertThatThrownBy(() -> registered().authenticate(new Password("password1"), HASHER))
                .isInstanceOf(UserUnconfirmedException.class);
    }

    @Test
    void anUnconfirmedAccountIsNotRevealedWithoutItsPassword() {
        assertThatThrownBy(() -> registered().authenticate(new Password("password2"), HASHER))
                .isInstanceOf(CredentialsInvalidException.class);
    }

    @Test
    void verifyingThePasswordIgnoresConfirmation() {
        assertThatCode(() -> registered().verifyPassword(new Password("password1"), HASHER))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> registered().verifyPassword(new Password("password2"), HASHER))
                .isInstanceOf(CredentialsInvalidException.class);
    }

    @Test
    void anAccountIsAbandonedOnceItsLinkExpiredUnused() {
        assertThat(registered().isAbandoned(NOW.plus(Duration.ofDays(7)).minusSeconds(1)))
                .isFalse();
        assertThat(registered().isAbandoned(NOW.plus(Duration.ofDays(7)))).isTrue();
        assertThat(confirmed().isAbandoned(NOW.plus(Duration.ofDays(70)))).isFalse();
    }

    private static User registered() {
        return User.register(
                new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                new UserName("Zed"),
                new Email("zed@example.com"),
                HASHER.hash(new Password("password1")),
                TOKEN,
                NOW,
                InterfaceLanguage.EN);
    }

    private static User confirmed() {
        User user = registered();
        user.confirm(TOKEN, NOW);
        return user;
    }
}

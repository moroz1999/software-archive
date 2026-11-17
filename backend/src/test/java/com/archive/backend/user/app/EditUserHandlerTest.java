package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.confirmed;
import static com.archive.backend.user.app.UserFixtures.id;
import static com.archive.backend.user.app.UserFixtures.unconfirmed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.LastAdministratorException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UnconfirmedUserGroupsException;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserEditing;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserNotFoundException;
import com.archive.backend.user.domain.UserStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Set;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditUserHandlerTest {

    private static final GrantedGroups MODERATOR = new GrantedGroups(Set.of(UserGroup.MODERATOR));
    private static final GrantedGroups ADMIN_GROUP = new GrantedGroups(Set.of(UserGroup.ADMIN));
    private static final GrantedGroups NONE = new GrantedGroups(Set.of());

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final EditUserHandler handler =
            new EditUserHandler(users, new UserEditing(users), Clock.fixed(NOW, ZoneOffset.UTC));

    private User admin;
    private User zed;

    @BeforeEach
    void storeUsers() {
        admin = confirmed(1, "Admin", UserGroup.ADMIN);
        zed = confirmed(2, "Zed");
        users.save(admin);
        users.save(zed);
    }

    @Test
    void anAdministratorGivesAUserANewNameEmailAndGroups() {
        handler.handle(edit(admin.actor(), 2, "Axe", "axe@example.com", MODERATOR));

        User user = stored(id(2));
        assertThat(user.getName()).isEqualTo(new UserName("Axe"));
        assertThat(user.getEmail()).isEqualTo(new Email("axe@example.com"));
        assertThat(user.getGroups()).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.MODERATOR);
    }

    @Test
    void anAdministratorMakesAnotherUserAnAdministrator() {
        handler.handle(edit(
                admin.actor(),
                2,
                "Zed",
                "zed@example.com",
                new GrantedGroups(Set.of(UserGroup.ADMIN, UserGroup.MODERATOR))));

        assertThat(stored(id(2)).getGroups())
                .containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN, UserGroup.MODERATOR);
    }

    @Test
    void aUserKeepsTheirOwnNameAndEmailInAnotherLetterCase() {
        handler.handle(edit(admin.actor(), 2, "ZED", "ZED@example.com", NONE));

        User user = stored(id(2));
        assertThat(user.getName()).isEqualTo(new UserName("ZED"));
        assertThat(user.getEmail()).isEqualTo(new Email("zed@example.com"));
    }

    @Test
    void refusesANameTakenByAnotherAccountWhateverItsCase() {
        assertRefused(
                () -> handler.handle(edit(admin.actor(), 2, "ADMIN", "zed@example.com", NONE)),
                "user.name-taken",
                "name");
        assertThat(stored(id(2)).getName()).isEqualTo(new UserName("Zed"));
    }

    @Test
    void refusesAnEmailTakenByAnotherAccount() {
        assertRefused(
                () -> handler.handle(edit(admin.actor(), 2, "Zed", "Admin@example.com", NONE)),
                "user.email-taken",
                "email");
        assertThat(stored(id(2)).getEmail()).isEqualTo(new Email("zed@example.com"));
    }

    @Test
    void anAccountWaitingForItsLinkKeepsItsNameAndEmail() {
        users.save(unconfirmed(3, "Bob", new ConfirmationToken("token-3"), NOW.minus(Duration.ofDays(6))));

        assertRefused(
                () -> handler.handle(edit(admin.actor(), 2, "Bob", "zed@example.com", NONE)),
                "user.name-taken",
                "name");
        assertRefused(
                () -> handler.handle(edit(admin.actor(), 2, "Zed", "bob@example.com", NONE)),
                "user.email-taken",
                "email");
    }

    @Test
    void anAbandonedAccountGivesUpItsNameAndEmail() {
        users.save(unconfirmed(3, "Bob", new ConfirmationToken("token-3"), NOW.minus(Duration.ofDays(8))));
        users.save(unconfirmed(4, "Cid", new ConfirmationToken("token-4"), NOW.minus(Duration.ofDays(8))));

        handler.handle(edit(admin.actor(), 2, "Bob", "cid@example.com", NONE));

        assertThat(users.findById(id(3))).isEmpty();
        assertThat(users.findById(id(4))).isEmpty();
        assertThat(stored(id(2)).getName()).isEqualTo(new UserName("Bob"));
        assertThat(stored(id(2)).getEmail()).isEqualTo(new Email("cid@example.com"));
    }

    @Test
    void anAdministratorStopsBeingOneWhileAnotherIsLeft() {
        zed.grant(UserGroup.ADMIN);

        handler.handle(edit(admin.actor(), 1, "Admin", "admin@example.com", NONE));

        assertThat(stored(id(1)).getGroups()).containsExactly(UserGroup.MEMBER);
    }

    @Test
    void theLastAdministratorStaysOne() {
        assertThatThrownBy(() -> handler.handle(edit(admin.actor(), 1, "Boss", "admin@example.com", MODERATOR)))
                .isInstanceOf(LastAdministratorException.class)
                .hasFieldOrPropertyWithValue("code", "user.last-admin");
        User user = stored(id(1));
        assertThat(user.getGroups()).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN);
        assertThat(user.getName()).isEqualTo(new UserName("Admin"));
    }

    @Test
    void theLastAdministratorRenamesThemselves() {
        handler.handle(edit(admin.actor(), 1, "Boss", "boss@example.com", ADMIN_GROUP));

        User user = stored(id(1));
        assertThat(user.getName()).isEqualTo(new UserName("Boss"));
        assertThat(user.getGroups()).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN);
    }

    @Test
    void anUnconfirmedAccountGetsANewNameAndEmailButNoGroups() {
        users.save(unconfirmed(3, "Bob", new ConfirmationToken("token-3"), NOW));

        handler.handle(edit(admin.actor(), 3, "Rob", "rob@example.com", NONE));

        User user = stored(id(3));
        assertThat(user.getName()).isEqualTo(new UserName("Rob"));
        assertThat(user.getEmail()).isEqualTo(new Email("rob@example.com"));
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
        assertThat(user.getGroups()).isEmpty();
    }

    @Test
    void anUnconfirmedAccountGetsNoGroups() {
        users.save(unconfirmed(3, "Bob", new ConfirmationToken("token-3"), NOW));

        assertThatThrownBy(() -> handler.handle(edit(admin.actor(), 3, "Rob", "rob@example.com", MODERATOR)))
                .isInstanceOf(UnconfirmedUserGroupsException.class)
                .hasFieldOrPropertyWithValue("code", "user.unconfirmed-groups");
        User user = stored(id(3));
        assertThat(user.getGroups()).isEmpty();
        assertThat(user.getName()).isEqualTo(new UserName("Bob"));
    }

    @Test
    void aMemberEditsNobody() {
        assertThatThrownBy(() -> handler.handle(edit(zed.actor(), 2, "Axe", "zed@example.com", NONE)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(stored(id(2)).getName()).isEqualTo(new UserName("Zed"));
    }

    @Test
    void aModeratorEditsNobody() {
        Actor moderator = new Actor(id(5), Set.of(UserGroup.MEMBER, UserGroup.MODERATOR));

        assertThatThrownBy(() -> handler.handle(edit(moderator, 2, "Axe", "zed@example.com", NONE)))
                .isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void aPublicVisitorIsToldToSignIn() {
        assertThatThrownBy(() -> handler.handle(edit(Actor.anonymous(), 2, "Axe", "zed@example.com", NONE)))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void anUnknownUserIsNotFound() {
        assertThatThrownBy(() -> handler.handle(edit(admin.actor(), 9, "Axe", "axe@example.com", NONE)))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "user.not-found");
    }

    private static EditUserCommand edit(Actor actor, int n, String name, String email, GrantedGroups groups) {
        return new EditUserCommand(actor, id(n), new UserName(name), new Email(email), groups);
    }

    private User stored(UserId id) {
        return users.findById(id).orElseThrow();
    }

    private static void assertRefused(ThrowingCallable call, String code, String field) {
        assertThatThrownBy(call).isInstanceOfSatisfying(RuleViolationException.class, e -> {
            assertThat(e.code()).isEqualTo(code);
            assertThat(e.field()).isEqualTo(field);
        });
    }
}

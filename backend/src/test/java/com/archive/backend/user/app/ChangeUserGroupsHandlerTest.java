package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.confirmed;
import static com.archive.backend.user.app.UserFixtures.id;
import static com.archive.backend.user.app.UserFixtures.unconfirmed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.GroupAssignment;
import com.archive.backend.user.domain.LastAdministratorException;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.user.domain.UnconfirmedUserGroupsException;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserNotFoundException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChangeUserGroupsHandlerTest {

    private static final GrantedGroups MODERATOR = new GrantedGroups(Set.of(UserGroup.MODERATOR));
    private static final GrantedGroups NONE = new GrantedGroups(Set.of());

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final ChangeUserGroupsHandler handler = new ChangeUserGroupsHandler(users, new GroupAssignment(users));

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
    void anAdministratorGivesAUserTheGroupsChosen() {
        handler.handle(new ChangeUserGroupsCommand(admin.actor(), id(2), MODERATOR));

        assertThat(groupsOf(id(2))).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.MODERATOR);
    }

    @Test
    void anAdministratorMakesAnotherUserAnAdministrator() {
        handler.handle(new ChangeUserGroupsCommand(
                admin.actor(), id(2), new GrantedGroups(Set.of(UserGroup.ADMIN, UserGroup.MODERATOR))));

        assertThat(groupsOf(id(2))).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN, UserGroup.MODERATOR);
    }

    @Test
    void anAdministratorStopsBeingOneWhileAnotherIsLeft() {
        zed.grant(UserGroup.ADMIN);

        handler.handle(new ChangeUserGroupsCommand(admin.actor(), id(1), NONE));

        assertThat(groupsOf(id(1))).containsExactly(UserGroup.MEMBER);
    }

    @Test
    void theLastAdministratorStaysOne() {
        assertThatThrownBy(() -> handler.handle(new ChangeUserGroupsCommand(admin.actor(), id(1), MODERATOR)))
                .isInstanceOf(LastAdministratorException.class)
                .hasFieldOrPropertyWithValue("code", "user.last-admin");
        assertThat(groupsOf(id(1))).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN);
    }

    @Test
    void theLastAdministratorKeepsTheirGroupWhileOtherGroupsChange() {
        handler.handle(new ChangeUserGroupsCommand(
                admin.actor(), id(1), new GrantedGroups(Set.of(UserGroup.ADMIN, UserGroup.MODERATOR))));

        assertThat(groupsOf(id(1))).containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.ADMIN, UserGroup.MODERATOR);
    }

    @Test
    void aMemberGivesNoGroups() {
        assertThatThrownBy(() -> handler.handle(new ChangeUserGroupsCommand(zed.actor(), id(2), MODERATOR)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(groupsOf(id(2))).containsExactly(UserGroup.MEMBER);
    }

    @Test
    void aModeratorGivesNoGroups() {
        Actor moderator = new Actor(id(3), Set.of(UserGroup.MEMBER, UserGroup.MODERATOR));

        assertThatThrownBy(() -> handler.handle(new ChangeUserGroupsCommand(moderator, id(2), MODERATOR)))
                .isInstanceOf(ChangeForbiddenException.class);
    }

    @Test
    void aPublicVisitorIsToldToSignIn() {
        assertThatThrownBy(() -> handler.handle(new ChangeUserGroupsCommand(Actor.anonymous(), id(2), MODERATOR)))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void anUnknownUserIsNotFound() {
        assertThatThrownBy(() -> handler.handle(new ChangeUserGroupsCommand(admin.actor(), id(9), MODERATOR)))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "user.not-found");
    }

    @Test
    void anUnconfirmedAccountGetsNoGroups() {
        users.save(unconfirmed(4, "Axe", new ConfirmationToken("token-4"), NOW));

        assertThatThrownBy(() -> handler.handle(new ChangeUserGroupsCommand(admin.actor(), id(4), MODERATOR)))
                .isInstanceOf(UnconfirmedUserGroupsException.class);
        assertThat(groupsOf(id(4))).isEmpty();
    }

    private Set<UserGroup> groupsOf(UserId id) {
        return users.findById(id).orElseThrow().getGroups();
    }
}

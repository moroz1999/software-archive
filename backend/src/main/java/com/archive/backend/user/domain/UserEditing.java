package com.archive.backend.user.domain;

import java.time.Instant;

/**
 * Changes accounts on an administrator's behalf, keeping the archive at least one administrator
 * (docs/domain/users.md).
 */
public final class UserEditing {

    private final UserRepository users;
    private final AccountNames names;

    public UserEditing(UserRepository users) {
        this.users = users;
        this.names = new AccountNames(users);
    }

    /**
     * The user with the name, email and groups given, not stored yet; abandoned accounts in its way are deleted.
     */
    public User edit(Actor actor, UserId id, UserName name, Email email, GrantedGroups groups, Instant now) {
        actor.requireAllowed(actor.mayManageUsers());
        User user = users.findById(id).orElseThrow(UserNotFoundException::new);
        boolean losesAdmin = user.getGroups().contains(UserGroup.ADMIN) && !groups.contains(UserGroup.ADMIN);
        if (losesAdmin && !users.hasAdminBesides(id)) {
            throw new LastAdministratorException();
        }
        names.claim(name, email, id, now);
        user.changeGroups(groups);
        user.rename(name, email);
        return user;
    }
}

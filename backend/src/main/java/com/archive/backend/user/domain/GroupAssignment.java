package com.archive.backend.user.domain;

/**
 * Gives users their groups on an administrator's behalf, keeping the archive at least one administrator.
 */
public final class GroupAssignment {

    private final UserRepository users;

    public GroupAssignment(UserRepository users) {
        this.users = users;
    }

    /**
     * The user with the groups given, not stored yet.
     */
    public User assign(Actor actor, UserId id, GrantedGroups groups) {
        actor.requireAllowed(actor.mayManageUsers());
        User user = users.findById(id).orElseThrow(UserNotFoundException::new);
        boolean losesAdmin = user.getGroups().contains(UserGroup.ADMIN) && !groups.contains(UserGroup.ADMIN);
        if (losesAdmin && !users.hasAdminBesides(id)) {
            throw new LastAdministratorException();
        }
        user.changeGroups(groups);
        return user;
    }
}

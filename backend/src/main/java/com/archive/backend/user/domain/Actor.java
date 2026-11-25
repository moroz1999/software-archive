package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Who makes a request: a signed-in user with their groups, or a public visitor.
 */
@ValueObject
public record Actor(@Nullable UserId id, Set<UserGroup> groups) {

    public Actor {
        groups = Set.copyOf(groups);
    }

    public static Actor anonymous() {
        return new Actor(null, Set.of(UserGroup.PUBLIC_VISITOR));
    }

    /**
     * The signed-in user; a public visitor is told to sign in first.
     */
    public UserId signedIn() {
        if (id == null) {
            throw new SignInRequiredException();
        }
        return id;
    }

    public boolean isAdministrator() {
        return groups.contains(UserGroup.ADMIN);
    }

    /**
     * The signed-in administrator; a public visitor is told to sign in first, anybody else is refused.
     */
    public UserId administrator() {
        requireAllowed(isAdministrator());
        return signedIn();
    }

    /**
     * Whether they may add software, releases, people and groups: every signed-in user.
     */
    public boolean mayAddEntries() {
        return id != null;
    }

    /**
     * Whether they may see the users of the archive and give them groups: administrators only.
     */
    public boolean mayManageUsers() {
        return isAdministrator();
    }

    /**
     * Whether they may keep the categories: administrators only.
     */
    public boolean mayKeepCategories() {
        return isAdministrator();
    }

    /**
     * Whether they may keep the hardware reference book and its groups: administrators only.
     */
    public boolean mayKeepHardware() {
        return isAdministrator();
    }

    /**
     * Whether they may talk to the AI assistant: the members of its group only.
     */
    public boolean mayUseAssistant() {
        return id != null && groups.contains(UserGroup.AI_ASSISTANT);
    }

    /**
     * Whether they may keep the countries and the cities: administrators only.
     */
    public boolean mayKeepPlaces() {
        return isAdministrator();
    }

    /**
     * Lets an action through only when it is allowed: otherwise a public visitor is told to sign in, and a signed-in
     * user that they may not.
     */
    public void requireAllowed(boolean allowed) {
        if (!allowed) {
            signedIn();
            throw new ChangeForbiddenException();
        }
    }
}

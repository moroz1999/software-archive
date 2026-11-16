package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Who added an entry of the archive. A new or changed entry always has its adder signed in; an entry whose adder's
 * account was deleted is only read without one (docs/domain/users.md).
 */
@ValueObject
public record AddedBy(@Nullable UserId user) {

    public static final AddedBy ACCOUNT_DELETED = new AddedBy(null);

    /**
     * The adder of an entry being added now: only a signed-in user adds entries.
     */
    public static AddedBy of(Actor actor) {
        return new AddedBy(actor.signedIn());
    }

    /**
     * Who added an entry, as the read side tells it: by the id of their account, or none once it was deleted.
     */
    public static AddedBy of(@Nullable UUID user) {
        return user == null ? ACCOUNT_DELETED : new AddedBy(new UserId(user));
    }

    /**
     * Whether this actor added the entry.
     */
    public boolean isBy(Actor actor) {
        return user != null && user.equals(actor.id());
    }

    /**
     * Whether this actor may change the entry: whoever added it and administrators.
     */
    public boolean mayBeChangedBy(Actor actor) {
        return actor.isAdministrator() || isBy(actor);
    }

    /**
     * Whether this actor may change an entry inside another, such as a release inside its software: also whoever added
     * the other.
     */
    public boolean mayBeChangedBy(Actor actor, AddedBy whole) {
        return mayBeChangedBy(actor) || whole.isBy(actor);
    }

    /**
     * Whoever added the entry may change it, and administrators may change any; an entry whose adder's account was deleted only they.
     */
    public void requireChangeableBy(Actor actor) {
        actor.requireAllowed(mayBeChangedBy(actor));
    }
}

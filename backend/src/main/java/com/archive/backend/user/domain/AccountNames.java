package com.archive.backend.user.domain;

import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Keeps a user name and an email to one account each, whatever their letter case; an abandoned account gives them up
 * (docs/domain/users.md).
 */
final class AccountNames {

    private final UserRepository users;

    AccountNames(UserRepository users) {
        this.users = users;
    }

    /**
     * Checks the name and the email are free for the account, a new one when none is given, and deletes the abandoned
     * accounts holding them.
     */
    void claim(UserName name, Email email, @Nullable UserId owner, Instant now) {
        Optional<User> nameHolder = users.findByName(name).filter(holder -> !isOwner(holder, owner));
        Optional<User> emailHolder = users.findByEmail(email).filter(holder -> !isOwner(holder, owner));
        if (nameHolder.filter(holder -> !holder.isAbandoned(now)).isPresent()) {
            throw new UserNameTakenException();
        }
        if (emailHolder.filter(holder -> !holder.isAbandoned(now)).isPresent()) {
            throw new UserEmailTakenException();
        }
        nameHolder.ifPresent(users::delete);
        emailHolder
                .filter(holder -> nameHolder
                        .map(User::getId)
                        .filter(holder.getId()::equals)
                        .isEmpty())
                .ifPresent(users::delete);
    }

    private static boolean isOwner(User holder, @Nullable UserId owner) {
        return holder.getId().equals(owner);
    }
}

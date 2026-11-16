package com.archive.backend.user.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Registers accounts: a user name and an email belong to one account only, and an abandoned account gives them up
 * (docs/domain/users.md).
 */
public final class Registrations {

    private final UserRepository users;
    private final PasswordHasher hasher;

    public Registrations(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    /**
     * The new account, not stored yet; abandoned accounts in its way are deleted.
     */
    public User register(UserName name, Email email, Password password, Instant now, InterfaceLanguage language) {
        claim(name, email, now);
        return User.register(
                new UserId(UUID.randomUUID()),
                name,
                email,
                hasher.hash(password),
                ConfirmationToken.generate(),
                now,
                language);
    }

    /**
     * The first administrator of an archive that has none, not stored yet; abandoned accounts in its way are deleted.
     */
    public User registerAdministrator(UserName name, Email email, Password password, Instant now) {
        if (users.hasAdmin()) {
            throw new AdministratorExistsException();
        }
        claim(name, email, now);
        return User.createAdministrator(new UserId(UUID.randomUUID()), name, email, hasher.hash(password), now);
    }

    /**
     * Checks the name and the email are free and deletes the abandoned accounts holding them.
     */
    private void claim(UserName name, Email email, Instant now) {
        Optional<User> nameHolder = users.findByName(name);
        Optional<User> emailHolder = users.findByEmail(email);
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
}

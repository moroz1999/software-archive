package com.archive.backend.user.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Registers accounts (docs/domain/users.md).
 */
public final class Registrations {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final AccountNames names;

    public Registrations(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
        this.names = new AccountNames(users);
    }

    /**
     * The new account, not stored yet; abandoned accounts in its way are deleted.
     */
    public User register(UserName name, Email email, Password password, Instant now, InterfaceLanguage language) {
        names.claim(name, email, null, now);
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
        names.claim(name, email, null, now);
        return User.createAdministrator(new UserId(UUID.randomUUID()), name, email, hasher.hash(password), now);
    }
}

package com.archive.backend.user.domain;

import java.util.Optional;

/**
 * Accounts as domain entities, for the use cases that change them.
 */
public interface UserRepository {

    Optional<User> findById(UserId id);

    /** The account whose user name or email is the login, whatever the letter case. */
    Optional<User> findByLogin(Login login);

    Optional<User> findByName(UserName name);

    Optional<User> findByEmail(Email email);

    Optional<User> findByConfirmation(TokenDigest digest);

    boolean hasAdmin();

    void save(User user);

    void delete(User user);
}

package com.archive.backend.user.domain;

import java.time.Instant;

/**
 * Confirms an account by its link; the first confirmed account of the archive becomes an administrator.
 */
public final class AccountConfirmation {

    private final UserRepository users;

    public AccountConfirmation(UserRepository users) {
        this.users = users;
    }

    /**
     * The confirmed account, not stored yet.
     */
    public User confirm(ConfirmationToken token, Instant now) {
        User user = users.findByConfirmation(token.digest()).orElseThrow(ConfirmationInvalidException::new);
        user.confirm(token, now);
        if (!users.hasAdmin()) {
            user.grant(UserGroup.ADMIN);
        }
        return user;
    }
}

package com.archive.backend.user.domain;

/**
 * Turns a password into what is kept of it and checks a password against it.
 */
public interface PasswordHasher {

    PasswordHash hash(Password password);

    boolean matches(Password password, PasswordHash hash);
}

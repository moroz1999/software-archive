package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.AuthenticationRequiredException;

/**
 * The request needs a signed-in user, and its access token names none.
 */
public class SignInRequiredException extends AuthenticationRequiredException {

    private static final long serialVersionUID = 1L;

    public SignInRequiredException() {
        super("auth.required", "Sign in first.");
    }
}

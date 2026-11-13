package com.archive.backend.shared.domain.error;

/**
 * The request needs a signed-in user, and there is none: never signed in, or the session is over.
 */
public class AuthenticationRequiredException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AuthenticationRequiredException(String code, String message) {
        super(code, message);
    }
}

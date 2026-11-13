package com.archive.backend.user.domain.session;

import com.archive.backend.shared.domain.error.AuthenticationRequiredException;

public class SessionExpiredException extends AuthenticationRequiredException {

    private static final long serialVersionUID = 1L;

    public SessionExpiredException() {
        super("auth.session-expired", "The session is over.");
    }
}

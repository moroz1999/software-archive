package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class UserEmailTakenException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public UserEmailTakenException() {
        super("user.email-taken", "The email is taken.", "email");
    }
}

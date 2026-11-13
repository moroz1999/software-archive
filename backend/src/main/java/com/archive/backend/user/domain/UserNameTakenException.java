package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class UserNameTakenException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public UserNameTakenException() {
        super("user.name-taken", "The user name is taken.", "name");
    }
}

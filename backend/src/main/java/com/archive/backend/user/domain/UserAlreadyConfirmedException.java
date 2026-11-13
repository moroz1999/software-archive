package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class UserAlreadyConfirmedException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public UserAlreadyConfirmedException() {
        super("user.already-confirmed", "The account is already confirmed.");
    }
}

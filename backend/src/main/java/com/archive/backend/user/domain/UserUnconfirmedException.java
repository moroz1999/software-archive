package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class UserUnconfirmedException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public UserUnconfirmedException() {
        super("user.unconfirmed", "The account is not confirmed yet.");
    }
}

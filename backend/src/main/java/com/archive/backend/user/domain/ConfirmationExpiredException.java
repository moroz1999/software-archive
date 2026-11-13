package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class ConfirmationExpiredException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ConfirmationExpiredException() {
        super("user.confirmation-expired", "The confirmation link has expired.");
    }
}

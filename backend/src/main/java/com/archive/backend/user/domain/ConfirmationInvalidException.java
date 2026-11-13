package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class ConfirmationInvalidException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ConfirmationInvalidException() {
        super("user.confirmation-invalid", "The confirmation link is not valid.");
    }
}

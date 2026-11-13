package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class CredentialsInvalidException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CredentialsInvalidException() {
        super("user.credentials-invalid", "Wrong login or password.");
    }
}

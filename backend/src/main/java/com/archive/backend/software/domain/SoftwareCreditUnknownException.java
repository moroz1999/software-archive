package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class SoftwareCreditUnknownException extends RuleViolationException {
    private static final long serialVersionUID = 1L;

    public SoftwareCreditUnknownException() {
        super("software.credit-unknown", "A credited person is not in the archive.", "credits");
    }
}

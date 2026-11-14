package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class SoftwareCreditUnknownException extends RuleViolationException {
    private static final long serialVersionUID = 1L;

    public SoftwareCreditUnknownException() {
        super("software.credit-unknown", "Someone credited is not in the archive.", "credits");
    }
}

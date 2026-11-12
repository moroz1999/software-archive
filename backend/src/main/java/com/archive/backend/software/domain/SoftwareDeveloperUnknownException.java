package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A developer company chosen for software is not in the archive. */
public class SoftwareDeveloperUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public SoftwareDeveloperUnknownException() {
        super("software.developer-unknown", "The developer company is not in the archive.", "developers");
    }
}

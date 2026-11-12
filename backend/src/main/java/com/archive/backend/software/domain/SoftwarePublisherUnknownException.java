package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A publisher chosen for software is not in the archive. */
public class SoftwarePublisherUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public SoftwarePublisherUnknownException() {
        super("software.publisher-unknown", "The publisher is not in the archive.", "publishers");
    }
}

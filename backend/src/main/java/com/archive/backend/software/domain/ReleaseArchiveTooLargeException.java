package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class ReleaseArchiveTooLargeException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ReleaseArchiveTooLargeException() {
        super("release.archive-too-large", "A release archive must be at most 100 MB.", "archive");
    }
}

package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class ReleaseArchiveNameInvalidException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ReleaseArchiveNameInvalidException() {
        super("release.archive-name-invalid", "A release archive must have a file name.", "archive");
    }
}

package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class ReleaseArchiveChangeInvalidException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ReleaseArchiveChangeInvalidException() {
        super(
                "release.archive-change-invalid",
                "A release archive cannot be replaced and removed together.",
                "archive");
    }
}

package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class ScreenshotTooLargeException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ScreenshotTooLargeException() {
        super("screenshot.too-large", "A screenshot must be at most 10 MB.", "files");
    }
}

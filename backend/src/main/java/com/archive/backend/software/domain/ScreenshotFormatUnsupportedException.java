package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** The uploaded file is not a PNG, JPEG, GIF or WebP picture. */
public class ScreenshotFormatUnsupportedException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ScreenshotFormatUnsupportedException() {
        super("screenshot.format-unsupported", "A screenshot must be a PNG, JPEG, GIF or WebP picture.", "files");
    }
}

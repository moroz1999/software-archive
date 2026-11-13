package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class ScreenshotNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public ScreenshotNotFoundException() {
        super("screenshot.not-found", "Screenshot not found.");
    }
}

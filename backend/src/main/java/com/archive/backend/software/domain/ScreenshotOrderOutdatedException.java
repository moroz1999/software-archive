package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.ConflictException;

/** The order does not name exactly the screenshots there are: they changed since it was chosen. */
public class ScreenshotOrderOutdatedException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public ScreenshotOrderOutdatedException() {
        super("screenshot.order-outdated", "The screenshots have changed; choose their order again.");
    }
}

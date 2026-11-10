package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;

/**
 * A platform the archive does not know (docs/domain/hardware.md).
 */
public class PlatformUnknownException extends InvalidValueException {

    private static final long serialVersionUID = 1L;

    public PlatformUnknownException() {
        super("software.platform-unknown", "The platform is unknown.");
    }
}

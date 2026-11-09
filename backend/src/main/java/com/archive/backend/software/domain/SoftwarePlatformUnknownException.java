package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;

/** Software was given a platform the archive does not know (docs/domain/hardware.md). */
public class SoftwarePlatformUnknownException extends InvalidValueException {

    private static final long serialVersionUID = 1L;

    public SoftwarePlatformUnknownException() {
        super("software.platform-unknown", "The platform is unknown.");
    }
}

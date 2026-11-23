package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A platform gone from the archive in the meantime. */
public class HardwarePlatformUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public HardwarePlatformUnknownException() {
        super("hardware.platform-unknown", "A platform is not in the archive.", "platforms");
    }
}

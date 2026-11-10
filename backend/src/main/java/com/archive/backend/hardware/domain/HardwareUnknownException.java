package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * Hardware that is not in the reference book or not of the platform it is chosen for (docs/domain/hardware.md).
 */
public class HardwareUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public HardwareUnknownException() {
        super("hardware.unknown", "The hardware is not of the platform of the software.", "hardware");
    }
}

package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A platform taken away from hardware that software of it has (docs/domain/hardware.md#hardware). */
public class HardwarePlatformInUseException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public HardwarePlatformInUseException() {
        super("hardware.platform-in-use", "Software of a platform taken away has the hardware.", "platforms");
    }
}

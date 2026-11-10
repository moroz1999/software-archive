package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * The platform of software cannot change while it or its releases have hardware (docs/domain/hardware.md).
 */
public class SoftwarePlatformLockedException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public SoftwarePlatformLockedException() {
        super(
                "software.platform-locked",
                "The platform cannot change while the software or its releases have hardware.",
                "platform");
    }
}

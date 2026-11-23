package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** Hardware software or a release has, kept in the reference book. */
public class HardwareInUseException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public HardwareInUseException() {
        super("hardware.in-use", "Software or releases have the hardware.", null);
    }
}

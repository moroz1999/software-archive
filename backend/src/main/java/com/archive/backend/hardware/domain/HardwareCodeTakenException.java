package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.ConflictException;

/** A code other hardware is known by already. */
public class HardwareCodeTakenException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public HardwareCodeTakenException() {
        super("hardware.code-taken", "Other hardware has the code already.", "code");
    }
}

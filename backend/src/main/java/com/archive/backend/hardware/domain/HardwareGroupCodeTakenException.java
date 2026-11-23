package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.ConflictException;

/** A code another group of hardware is known by already. */
public class HardwareGroupCodeTakenException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public HardwareGroupCodeTakenException() {
        super("hardware.group-code-taken", "Another group has the code already.", "code");
    }
}

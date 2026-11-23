package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

/** A group of hardware the reference book does not have. */
public class HardwareGroupNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public HardwareGroupNotFoundException() {
        super("hardware.group-not-found", "Hardware group not found.");
    }
}

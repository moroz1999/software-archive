package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

/** Hardware the reference book does not have. */
public class HardwareNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public HardwareNotFoundException() {
        super("hardware.not-found", "Hardware not found.");
    }
}

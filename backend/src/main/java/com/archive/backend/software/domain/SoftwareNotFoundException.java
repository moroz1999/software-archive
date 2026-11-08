package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class SoftwareNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public SoftwareNotFoundException() {
        super("software.not-found", "Software not found.");
    }
}

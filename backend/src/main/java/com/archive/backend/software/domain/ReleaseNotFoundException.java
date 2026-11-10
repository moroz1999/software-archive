package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class ReleaseNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public ReleaseNotFoundException() {
        super("release.not-found", "Release not found.");
    }
}

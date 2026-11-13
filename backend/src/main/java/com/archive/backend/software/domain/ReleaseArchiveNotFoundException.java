package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class ReleaseArchiveNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public ReleaseArchiveNotFoundException() {
        super("release.archive-not-found", "Release archive not found.");
    }
}

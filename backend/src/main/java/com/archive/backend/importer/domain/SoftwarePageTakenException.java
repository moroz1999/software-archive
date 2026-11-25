package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.error.ConflictException;

/** The page of another site is a source of other software already: the software is there, to be completed. */
public class SoftwarePageTakenException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public SoftwarePageTakenException() {
        super("software.page-taken", "The page is a source of other software already.");
    }
}

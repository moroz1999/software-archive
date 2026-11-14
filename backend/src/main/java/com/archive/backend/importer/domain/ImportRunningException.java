package com.archive.backend.importer.domain;

import com.archive.backend.shared.domain.error.ConflictException;

/** An import is running already: one at a time, so that two runs do not add the same entries. */
public class ImportRunningException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public ImportRunningException() {
        super("import.running", "An import is running already; start it again once it is done.");
    }
}

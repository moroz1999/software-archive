package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

/** The label is not in the archive. */
public class LabelNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public LabelNotFoundException() {
        super("label.not-found", "Label not found.");
    }
}

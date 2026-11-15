package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.ConflictException;

/** The label has got an owner meanwhile: only a label of nobody is attached to one. */
public class LabelOwnedException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public LabelOwnedException() {
        super("label.owned", "The label has an owner already.");
    }
}

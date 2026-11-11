package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * An owner with labels has exactly one display name (docs/domain/authors.md).
 */
public class LabelDisplayNameException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelDisplayNameException() {
        super("label.display-one", "Exactly one label is the display name.", "labels");
    }
}

package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A person can have at most one display name (docs/domain/authors.md).
 */
public class LabelDisplayNameException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelDisplayNameException() {
        super("label.display-one", "At most one label can be the display name.", "labels");
    }
}

package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A label is merged only into another label, never into itself. */
public class LabelMergedIntoItselfException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelMergedIntoItselfException() {
        super("label.merge-into-itself", "A label is not merged into itself.", "label");
    }
}

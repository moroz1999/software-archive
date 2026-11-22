package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A label of a person is never given to a group: only a label of nobody or of a group is merged into one or moved. */
public class LabelOfPersonException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelOfPersonException() {
        super("label.of-person", "A label of a person is not given to a group.");
    }
}

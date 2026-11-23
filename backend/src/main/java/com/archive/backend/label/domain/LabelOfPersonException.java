package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A label of a person is never handed over: only a label of nobody or of a group is merged or given to a group. */
public class LabelOfPersonException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelOfPersonException() {
        super("label.of-person", "A label of a person is not handed over.");
    }
}

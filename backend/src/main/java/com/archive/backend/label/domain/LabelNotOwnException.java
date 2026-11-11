package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * An owner was given a label that is not its own: labels of others are taken over only explicitly.
 */
public class LabelNotOwnException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelNotOwnException() {
        super("label.not-own", "The label belongs to somebody else.", "labels");
    }
}

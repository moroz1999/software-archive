package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** The person or group a label is attached to is not in the archive. */
public class LabelOwnerUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LabelOwnerUnknownException() {
        this("The owner is not in the archive.");
    }

    /** The same refusal, saying which entry is missing. */
    public LabelOwnerUnknownException(String message) {
        super("label.owner-unknown", message, "owner");
    }
}

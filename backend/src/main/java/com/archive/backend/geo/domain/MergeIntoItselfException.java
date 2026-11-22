package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class MergeIntoItselfException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public MergeIntoItselfException() {
        super("geo.merge-into-itself", "A country or a city is merged into another one.", "into");
    }
}

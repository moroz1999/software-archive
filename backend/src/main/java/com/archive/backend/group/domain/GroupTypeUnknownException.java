package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A group type that is not in the reference book.
 */
public class GroupTypeUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public GroupTypeUnknownException() {
        super("group.type-unknown", "The group type is not in the archive.", "types");
    }
}

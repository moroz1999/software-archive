package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A group has at least one type (docs/domain/authors.md#groups).
 */
public class GroupTypesRequiredException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public GroupTypesRequiredException() {
        super("group.types-required", "A group needs at least one type.", "types");
    }
}

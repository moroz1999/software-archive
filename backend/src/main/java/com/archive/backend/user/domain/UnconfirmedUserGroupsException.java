package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * Groups are given to confirmed accounts only.
 */
public class UnconfirmedUserGroupsException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public UnconfirmedUserGroupsException() {
        super("user.unconfirmed-groups", "An unconfirmed account gets no groups.");
    }
}

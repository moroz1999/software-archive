package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class GroupMemberUnknownException extends RuleViolationException {
    private static final long serialVersionUID = 1L;

    public GroupMemberUnknownException() {
        super("group.member-unknown", "A group member is not in the archive.", "members");
    }
}

package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class GroupNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public GroupNotFoundException() {
        super("group.not-found", "Group not found.");
    }
}

package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.ForbiddenException;

/** The entry may be changed only by whoever added it and by administrators (docs/domain/users.md). */
public class ChangeForbiddenException extends ForbiddenException {

    private static final long serialVersionUID = 1L;

    public ChangeForbiddenException() {
        super("access.denied", "Only whoever added this, or an administrator, may change it.");
    }
}

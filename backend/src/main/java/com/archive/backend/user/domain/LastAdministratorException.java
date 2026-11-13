package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * The archive keeps at least one administrator.
 */
public class LastAdministratorException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LastAdministratorException() {
        super("user.last-admin", "The last administrator of the archive stays one.");
    }
}

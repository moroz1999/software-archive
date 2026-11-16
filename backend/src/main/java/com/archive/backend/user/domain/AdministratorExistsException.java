package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * The first administrator is created from the command line only while the archive has none.
 */
public class AdministratorExistsException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public AdministratorExistsException() {
        super("user.admin-exists", "The archive already has an administrator.");
    }
}

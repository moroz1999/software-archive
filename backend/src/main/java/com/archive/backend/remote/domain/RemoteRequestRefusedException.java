package com.archive.backend.remote.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A request to another site that is not made, or is given up: an address inside the archive's own network, too many
 * redirects, a failed transfer, a file over its limit, a status other than success, content of a wrong kind.
 */
public class RemoteRequestRefusedException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public RemoteRequestRefusedException(String message) {
        super("remote.refused", message);
    }
}

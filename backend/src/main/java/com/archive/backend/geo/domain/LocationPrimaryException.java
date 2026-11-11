package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * An owner with locations has exactly one primary location (docs/domain/authors.md).
 */
public class LocationPrimaryException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LocationPrimaryException() {
        super("location.primary-one", "Exactly one location is primary.", "locations");
    }
}

package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/**
 * A country or city that is not in the reference book, or a city of another country than the one given.
 */
public class LocationUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public LocationUnknownException() {
        super("location.unknown", "The place is not in the archive.", "locations");
    }
}

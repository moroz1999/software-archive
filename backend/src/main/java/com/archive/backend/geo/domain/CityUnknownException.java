package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** The city chosen to merge into is not in the archive. */
public class CityUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CityUnknownException() {
        super("geo.city-unknown", "The city is not in the archive.", "into");
    }
}

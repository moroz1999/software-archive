package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class CountryUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CountryUnknownException() {
        super("geo.country-unknown", "The country is not in the archive.", "countryId");
    }
}

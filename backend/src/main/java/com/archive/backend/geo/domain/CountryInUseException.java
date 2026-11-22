package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class CountryInUseException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CountryInUseException() {
        super("geo.country-in-use", "The country has cities or places in it.", null);
    }
}

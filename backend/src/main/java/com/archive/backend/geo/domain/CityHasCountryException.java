package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class CityHasCountryException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CityHasCountryException() {
        super("geo.city-has-country", "The city is in a country.", null);
    }
}

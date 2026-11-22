package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class CityInUseException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CityInUseException() {
        super("geo.city-in-use", "Places are in the city.", null);
    }
}

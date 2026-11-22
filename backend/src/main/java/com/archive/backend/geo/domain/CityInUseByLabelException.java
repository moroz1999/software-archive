package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

public class CityInUseByLabelException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public CityInUseByLabelException() {
        super("geo.city-in-use-by-label", "An old record of a label names the city.", null);
    }
}

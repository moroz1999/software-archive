package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.InvalidValueException;

public class LocationEmptyException extends InvalidValueException {

    private static final long serialVersionUID = 1L;

    public LocationEmptyException() {
        super("location.empty", "A location needs a country or a city.");
    }
}

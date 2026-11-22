package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class CityNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public CityNotFoundException() {
        super("geo.city-not-found", "City not found.");
    }
}

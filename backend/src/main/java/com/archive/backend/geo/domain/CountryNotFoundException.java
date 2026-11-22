package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.error.NotFoundException;

public class CountryNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public CountryNotFoundException() {
        super("geo.country-not-found", "Country not found.");
    }
}

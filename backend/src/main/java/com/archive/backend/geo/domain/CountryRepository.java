package com.archive.backend.geo.domain;

import java.util.Optional;

/** Countries as domain entities, for the use cases that change them. Reads go through the read model. */
public interface CountryRepository {

    Optional<Country> findById(CountryId id);

    void save(Country country);

    void delete(Country country);
}

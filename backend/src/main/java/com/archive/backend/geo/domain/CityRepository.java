package com.archive.backend.geo.domain;

import java.util.Optional;

/** Cities as domain entities, for the use cases that change them. Reads go through the read model. */
public interface CityRepository {

    Optional<City> findById(CityId id);

    void save(City city);

    void delete(City city);
}

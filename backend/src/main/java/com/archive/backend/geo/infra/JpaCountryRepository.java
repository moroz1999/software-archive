package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.Coordinates;
import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.CountryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaCountryRepository implements CountryRepository {

    private final SpringDataCountryRepository rows;

    @Override
    public Optional<Country> findById(CountryId id) {
        return rows.findById(id.value())
                .map(entity -> new Country(
                        id,
                        GeoNamesRows.toNames(entity.getNames()),
                        GeoNamesRows.toCoordinates(entity.getLatitude(), entity.getLongitude())));
    }

    @Override
    public void save(Country country) {
        CountryEntity entity = rows.findById(country.getId().value()).orElseGet(CountryEntity::new);
        entity.setId(country.getId().value());
        entity.getNames().clear();
        entity.getNames().putAll(GeoNamesRows.toRows(country.getNames()));
        Coordinates coordinates = country.getCoordinates();
        entity.setLatitude(coordinates == null ? null : coordinates.latitude());
        entity.setLongitude(coordinates == null ? null : coordinates.longitude());
        rows.saveAndFlush(entity);
    }

    @Override
    public void delete(Country country) {
        rows.deleteById(country.getId().value());
        rows.flush();
    }
}

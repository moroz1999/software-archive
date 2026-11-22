package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.Coordinates;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.KnownCountry;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaCityRepository implements CityRepository {

    private final SpringDataCityRepository rows;

    @Override
    public Optional<City> findById(CityId id) {
        return rows.findById(id.value()).map(entity -> {
            UUID country = entity.getCountryId();
            return new City(
                    id,
                    GeoNamesRows.toNames(entity.getNames()),
                    country == null ? null : KnownCountry.restore(new CountryId(country)),
                    GeoNamesRows.toCoordinates(entity.getLatitude(), entity.getLongitude()));
        });
    }

    @Override
    public void save(City city) {
        CityEntity entity = rows.findById(city.getId().value()).orElseGet(CityEntity::new);
        entity.setId(city.getId().value());
        CountryId country = city.countryId();
        entity.setCountryId(country == null ? null : country.value());
        entity.getNames().clear();
        entity.getNames().putAll(GeoNamesRows.toRows(city.getNames()));
        Coordinates coordinates = city.getCoordinates();
        entity.setLatitude(coordinates == null ? null : coordinates.latitude());
        entity.setLongitude(coordinates == null ? null : coordinates.longitude());
        rows.saveAndFlush(entity);
    }

    @Override
    public void delete(City city) {
        rows.deleteById(city.getId().value());
        rows.flush();
    }
}

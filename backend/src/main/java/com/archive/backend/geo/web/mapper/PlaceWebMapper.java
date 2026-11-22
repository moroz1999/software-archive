package com.archive.backend.geo.web.mapper;

import com.archive.backend.geo.app.CreateCityCommand;
import com.archive.backend.geo.app.CreateCountryCommand;
import com.archive.backend.geo.app.UpdateCityCommand;
import com.archive.backend.geo.app.UpdateCountryCommand;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.Coordinates;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.geo.domain.GeoNames;
import com.archive.backend.geo.read.CityDetails;
import com.archive.backend.geo.read.CountryDetails;
import com.archive.backend.geo.read.GeographyCountry;
import com.archive.backend.geo.web.dto.CityDetailsDto;
import com.archive.backend.geo.web.dto.CountryDetailsDto;
import com.archive.backend.geo.web.dto.GeographyCountryDto;
import com.archive.backend.geo.web.dto.PlaceRequestDto;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

/** Countries and cities between the requests of the management and the use cases. */
@Mapper(componentModel = "spring")
public interface PlaceWebMapper {

    List<GeographyCountryDto> toGeography(List<GeographyCountry> countries);

    CountryDetailsDto toDetails(CountryDetails country);

    CityDetailsDto toDetails(CityDetails city);

    default CreateCountryCommand toCreateCountry(Actor actor, PlaceRequestDto request) {
        return new CreateCountryCommand(actor, toNames(request.names()), toCoordinates(request));
    }

    default UpdateCountryCommand toUpdateCountry(Actor actor, UUID id, PlaceRequestDto request) {
        return new UpdateCountryCommand(actor, new CountryId(id), toNames(request.names()), toCoordinates(request));
    }

    default CreateCityCommand toCreateCity(Actor actor, PlaceRequestDto request) {
        return new CreateCityCommand(
                actor, toNames(request.names()), toCountry(request.countryId()), toCoordinates(request));
    }

    default UpdateCityCommand toUpdateCity(Actor actor, UUID id, PlaceRequestDto request) {
        return new UpdateCityCommand(
                actor,
                new CityId(id),
                toNames(request.names()),
                toCountry(request.countryId()),
                toCoordinates(request));
    }

    /** A name left blank is not given. */
    private static GeoNames toNames(@Nullable Map<String, String> values) {
        Map<Language, GeoName> names = new HashMap<>();
        if (values != null) {
            values.forEach((code, name) -> {
                Language language = Language.known(code);
                if (name != null && !name.isBlank()) {
                    names.put(language, new GeoName(name));
                }
            });
        }
        return new GeoNames(names);
    }

    private static @Nullable Coordinates toCoordinates(PlaceRequestDto request) {
        Double latitude = request.latitude();
        Double longitude = request.longitude();
        if (latitude == null && longitude == null) {
            return null;
        }
        if (latitude == null || longitude == null) {
            throw Coordinates.invalid();
        }
        return new Coordinates(latitude, longitude);
    }

    private static @Nullable CountryId toCountry(@Nullable UUID value) {
        return value == null ? null : new CountryId(value);
    }
}

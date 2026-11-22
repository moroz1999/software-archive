package com.archive.backend.geo.web;

import com.archive.backend.geo.app.CreateCityHandler;
import com.archive.backend.geo.app.CreateCountryHandler;
import com.archive.backend.geo.app.DeleteCityCommand;
import com.archive.backend.geo.app.DeleteCityHandler;
import com.archive.backend.geo.app.DeleteCountryCommand;
import com.archive.backend.geo.app.DeleteCountryHandler;
import com.archive.backend.geo.app.GetCityHandler;
import com.archive.backend.geo.app.GetCityQuery;
import com.archive.backend.geo.app.GetCountryHandler;
import com.archive.backend.geo.app.GetCountryQuery;
import com.archive.backend.geo.app.ListGeographyHandler;
import com.archive.backend.geo.app.ListGeographyQuery;
import com.archive.backend.geo.app.UpdateCityHandler;
import com.archive.backend.geo.app.UpdateCountryHandler;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.web.dto.CityDetailsDto;
import com.archive.backend.geo.web.dto.CountryDetailsDto;
import com.archive.backend.geo.web.dto.GeographyCountryDto;
import com.archive.backend.geo.web.dto.PlaceCreatedDto;
import com.archive.backend.geo.web.dto.PlaceRequestDto;
import com.archive.backend.geo.web.mapper.PlaceWebMapper;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Keeping the countries and the cities from the management (docs/features/geography.md). */
@RestController
@RequiredArgsConstructor
public class GeographyController {

    private final ListGeographyHandler listHandler;
    private final GetCountryHandler getCountryHandler;
    private final CreateCountryHandler createCountryHandler;
    private final UpdateCountryHandler updateCountryHandler;
    private final DeleteCountryHandler deleteCountryHandler;
    private final GetCityHandler getCityHandler;
    private final CreateCityHandler createCityHandler;
    private final UpdateCityHandler updateCityHandler;
    private final DeleteCityHandler deleteCityHandler;
    private final PlaceWebMapper webMapper;

    /** Every country with its cities at once: the tree of the management needs all of it. */
    @GetMapping("/api/geography")
    public List<GeographyCountryDto> geography(Language language) {
        return webMapper.toGeography(listHandler.handle(new ListGeographyQuery(language)));
    }

    @GetMapping("/api/countries/{id}")
    public CountryDetailsDto country(@PathVariable UUID id) {
        return webMapper.toDetails(getCountryHandler.handle(new GetCountryQuery(new CountryId(id))));
    }

    @PostMapping("/api/countries")
    public ResponseEntity<PlaceCreatedDto> createCountry(Actor actor, @RequestBody PlaceRequestDto request) {
        UUID id = createCountryHandler
                .handle(webMapper.toCreateCountry(actor, request))
                .value();
        return ResponseEntity.created(URI.create("/api/countries/" + id)).body(new PlaceCreatedDto(id));
    }

    @PutMapping("/api/countries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCountry(Actor actor, @PathVariable UUID id, @RequestBody PlaceRequestDto request) {
        updateCountryHandler.handle(webMapper.toUpdateCountry(actor, id, request));
    }

    @DeleteMapping("/api/countries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCountry(Actor actor, @PathVariable UUID id) {
        deleteCountryHandler.handle(new DeleteCountryCommand(actor, new CountryId(id)));
    }

    @GetMapping("/api/cities/{id}")
    public CityDetailsDto city(@PathVariable UUID id) {
        return webMapper.toDetails(getCityHandler.handle(new GetCityQuery(new CityId(id))));
    }

    @PostMapping("/api/cities")
    public ResponseEntity<PlaceCreatedDto> createCity(Actor actor, @RequestBody PlaceRequestDto request) {
        UUID id =
                createCityHandler.handle(webMapper.toCreateCity(actor, request)).value();
        return ResponseEntity.created(URI.create("/api/cities/" + id)).body(new PlaceCreatedDto(id));
    }

    @PutMapping("/api/cities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCity(Actor actor, @PathVariable UUID id, @RequestBody PlaceRequestDto request) {
        updateCityHandler.handle(webMapper.toUpdateCity(actor, id, request));
    }

    @DeleteMapping("/api/cities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCity(Actor actor, @PathVariable UUID id) {
        deleteCityHandler.handle(new DeleteCityCommand(actor, new CityId(id)));
    }
}

package com.archive.backend.geo.web;

import com.archive.backend.geo.app.ListCitiesHandler;
import com.archive.backend.geo.app.ListCitiesQuery;
import com.archive.backend.geo.app.ListCountriesHandler;
import com.archive.backend.geo.app.ListCountriesQuery;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.web.dto.CityDto;
import com.archive.backend.geo.web.dto.CountryDto;
import com.archive.backend.geo.web.mapper.GeoWebMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class GeoController {

    private final ListCountriesHandler countriesHandler;
    private final ListCitiesHandler citiesHandler;
    private final GeoWebMapper webMapper;

    @GetMapping
    public List<CountryDto> countries() {
        return webMapper.toCountries(countriesHandler.handle(new ListCountriesQuery()));
    }

    @GetMapping("/{id}/cities")
    public List<CityDto> cities(@PathVariable UUID id) {
        return webMapper.toCities(citiesHandler.handle(new ListCitiesQuery(new CountryId(id))));
    }
}

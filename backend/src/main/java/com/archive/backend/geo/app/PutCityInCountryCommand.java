package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.user.domain.Actor;

public record PutCityInCountryCommand(Actor actor, CityId city, CountryId country) {}

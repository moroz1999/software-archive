package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.user.domain.Actor;

public record TurnCityIntoCountryCommand(Actor actor, CityId city) {}

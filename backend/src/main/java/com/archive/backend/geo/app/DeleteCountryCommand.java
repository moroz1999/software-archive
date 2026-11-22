package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.user.domain.Actor;

public record DeleteCountryCommand(Actor actor, CountryId id) {}

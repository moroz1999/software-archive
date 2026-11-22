package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.user.domain.Actor;

public record MergeCountryCommand(Actor actor, CountryId country, CountryId into) {}

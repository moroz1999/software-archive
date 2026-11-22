package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.Coordinates;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoNames;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

public record UpdateCityCommand(
        Actor actor,
        CityId id,
        GeoNames names,
        @Nullable CountryId country,
        @Nullable Coordinates coordinates) {}

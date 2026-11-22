package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.Coordinates;
import com.archive.backend.geo.domain.GeoNames;
import com.archive.backend.user.domain.Actor;
import org.jspecify.annotations.Nullable;

public record CreateCountryCommand(
        Actor actor, GeoNames names, @Nullable Coordinates coordinates) {}

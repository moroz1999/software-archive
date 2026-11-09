package com.archive.backend.geo.read;

import java.util.UUID;

/** A city as an option to choose within its country. */
public record CityItem(UUID id, String name) {}

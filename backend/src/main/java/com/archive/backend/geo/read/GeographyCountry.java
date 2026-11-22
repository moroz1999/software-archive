package com.archive.backend.geo.read;

import java.util.List;
import java.util.UUID;

/** A country of the archive with how many places are in it, in its cities too, and its cities. */
public record GeographyCountry(UUID id, String name, int places, List<GeographyCity> cities) {}

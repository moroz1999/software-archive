package com.archive.backend.geo.read;

import java.util.UUID;

/** A city of a country with how many places are in it. */
public record GeographyCity(UUID id, String name, int places) {}

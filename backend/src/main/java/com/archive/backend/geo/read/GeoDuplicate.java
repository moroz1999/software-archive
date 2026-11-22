package com.archive.backend.geo.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A country or a city that may be a duplicate: its name, a city's country, if known, and how many places are in it. */
public record GeoDuplicate(UUID id, String name, @Nullable String country, int places) {}

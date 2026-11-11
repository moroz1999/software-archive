package com.archive.backend.search.read;

import java.util.UUID;

/** A category or a country of the catalog's filter with the count of software it leaves. */
public record IdCount(UUID id, long count) {}

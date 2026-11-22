package com.archive.backend.importer.read;

/**
 * A place a run added.
 *
 * @param kind {@code city} or {@code country}
 */
public record ImportRunPlaceEntry(String kind, String name) implements ImportRunLogEntry {}

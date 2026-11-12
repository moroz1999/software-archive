package com.archive.backend.search.read;

import java.util.UUID;

/** A group found by the search, under the main name. */
public record FoundGroup(UUID id, String name) {}

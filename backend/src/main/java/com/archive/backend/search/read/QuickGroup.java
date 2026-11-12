package com.archive.backend.search.read;

import java.util.UUID;

/** A group found by the quick search, under the main name. */
public record QuickGroup(UUID id, String name) {}
